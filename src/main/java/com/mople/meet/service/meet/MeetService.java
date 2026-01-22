package com.mople.meet.service.meet;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.domain.meet.*;
import com.mople.dto.request.meet.HostChangeRequest;
import com.mople.dto.request.meet.MeetCreateRequest;
import com.mople.dto.request.meet.MeetUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.*;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.custom.UserCursor;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.entity.meet.*;
import com.mople.meet.repository.*;

import com.mople.meet.service.meet.member.MeetMemberAutoCompleteService;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.*;

import static com.mople.dto.client.MeetClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofMembers;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;
import static com.mople.global.utils.cursor.custom.UserCursor.forMeetMember;

@Service
public class MeetService {

    private static final int MEET_CURSOR_FIELD_COUNT = 1;
    private static final int MEET_MEMBER_CURSOR_FIELD_COUNT = 1;

    private final MeetRepository meetRepository;
    private final MeetMemberRepository meetMemberRepository;
    private final MeetInviteRepository meetInviteRepository;
    private final MeetRepositorySupport meetRepositorySupport;
    private final MeetMemberRepositorySupport meetMemberRepositorySupport;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
    private final MeetRemoveService meetRemoveService;
    private final MeetMemberAutoCompleteService autoCompleteService;
    private final MeetHostTransferService meetHostTransferService;
    private final EntityReader reader;

    private final String inviteUrl;

    public MeetService(
            MeetRepository meetRepository,
            MeetMemberRepository meetMemberRepository,
            MeetInviteRepository meetInviteRepository,
            MeetRepositorySupport meetRepositorySupport,
            MeetMemberRepositorySupport meetMemberRepositorySupport,
            UserRepository userRepository,
            OutboxService outboxService,
            MeetRemoveService meetRemoveService,
            MeetMemberAutoCompleteService autoCompleteService,
            MeetHostTransferService meetHostTransferService,
            EntityReader reader,
            @Value("${mople.url}") String inviteUrl
    ) {
        this.meetRepository = meetRepository;
        this.meetMemberRepository = meetMemberRepository;
        this.meetInviteRepository = meetInviteRepository;
        this.meetRepositorySupport = meetRepositorySupport;
        this.meetMemberRepositorySupport = meetMemberRepositorySupport;
        this.userRepository = userRepository;
        this.outboxService = outboxService;
        this.meetRemoveService = meetRemoveService;
        this.autoCompleteService = autoCompleteService;
        this.meetHostTransferService = meetHostTransferService;
        this.reader = reader;
        this.inviteUrl = inviteUrl;
    }

    @Transactional
    public MeetClientResponse createMeet(Long creatorId, MeetCreateRequest request) {
        var user = reader.findUser(creatorId);

        Meet meet =
                meetRepository.save(
                        Meet.builder()
                                .hostId(user.getId())
                                .meetImage(request.image())
                                .name(request.name())
                                .build()
                );

        meetMemberRepository.save(
                MeetMember.builder()
                        .meetId(meet.getId())
                        .userId(user.getId())
                        .nickName(user.getNickname())
                        .hostId(meet.getHostId())
                        .build()
        );

        Integer memberCount = meetRepositorySupport.countMeetMember(meet.getId());

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional
    public MeetClientResponse updateMeet(Long creatorId, Long meetId, MeetUpdateRequest request) {
        reader.findUser(creatorId);
        var meet = reader.findMeet(meetId);

        if (!meet.matchHost(creatorId)) {
            throw new AuthException(NOT_CREATOR);
        }

        String oldImage = meet.getMeetImage();
        meet.updateMeetInfo(request.name(), request.image());

        try {
            meetRepository.flush();

        } catch (
                OptimisticLockException
                | OptimisticLockingFailureException
                | StaleObjectStateException e
        ) {
            long currentVersion = meetRepository.findVersion(meet.getId());
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, currentVersion);
        }

        if ((oldImage != null && !oldImage.isBlank())
                && !Objects.equals(oldImage, request.image())) {

            MeetImageChangedEvent changedEvent = MeetImageChangedEvent.builder()
                    .meetId(meetId)
                    .imageUrl(oldImage)
                    .imageDeletedBy(creatorId)
                    .build();

            outboxService.save(MEET_IMAGE_CHANGED, MEET, meetId, changedEvent);
        }

        Integer memberCount = meetRepositorySupport.countMeetMember(meet.getId());

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<MeetClientResponse> getUserMeetList(Long userId, CursorPageRequest request) {
        reader.findUser(userId);

        int size = request.getSafeSize();
        List<Meet> meets = getMeets(userId, request.cursor(), size);

        List<MeetListResponse> meetListResponses = meetRepositorySupport.mapToMeetListResponses(meets);

        return buildMeetCursorPage(size, meetListResponses);
    }

    private List<Meet> getMeets(Long userId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validateCursor(cursorId);
        }

        return meetRepositorySupport.findMeetPage(userId, cursorId, size);
    }

    private void validateCursor(Long cursorId) {
        if (meetRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    private CursorPageResponse<MeetClientResponse> buildMeetCursorPage(int size, List<MeetListResponse> meetListResponses) {
        return buildCursorPage(
                meetListResponses,
                size,
                r -> new String[]{
                        r.meetId().toString()
                },
                MeetClientResponse::ofListMeets
        );
    }

    @Transactional(readOnly = true)
    public MeetClientResponse getMeetDetail(Long userId, Long meetId) {
        var meet = reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        Integer memberCount = meetRepositorySupport.countMeetMember(meetId);

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<UserRoleClientResponse> meetMemberList(Long userId, Long meetId, CursorPageRequest request) {
        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<MeetMember> meetMembers = getMeetMembers(meet.getId(), request.cursor(), size);

        Integer memberCount = meetRepositorySupport.countMeetMember(meetId);

        return FlatCursorPageResponse.of(
                memberCount,
                buildMemberCursorPage(size, meetMembers)
        );
    }

    private List<MeetMember> getMeetMembers(Long meetId, String encodedCursor, int size) {

        UserCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

            Long cursorId = Long.valueOf(decodeParts[0]);
            MeetMember member = meetMemberRepository.findById(cursorId)
                    .orElseThrow(() -> new CursorException(INVALID_CURSOR));

            if (!Objects.equals(member.getMeetId(), meetId)) {
                throw new CursorException(INVALID_CURSOR);
            }

            cursor = forMeetMember(member);
        }

        return meetMemberRepositorySupport.findMemberPage(meetId, cursor, size);
    }

    private CursorPageResponse<UserRoleClientResponse> buildMemberCursorPage(int size, List<MeetMember> members) {
        List<Long> userIds = members.stream()
                .map(MeetMember::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                members,
                size,
                m ->
                        new String[]{
                            m.getId().toString()
                        },
                list -> ofMembers(list, userInfoById)
        );
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserRoleClientResponse> searchMeetMember(Long userId, Long meetId, String keyword, CursorPageRequest request) {
        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new BadRequestException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<MeetMember> meetMembers = autoCompleteService.getMeetMembers(
                meetId,
                keyword.toLowerCase(),
                request.cursor(),
                size
        );

        return autoCompleteService.buildAutoCompleteCursorPage(size, meetMembers);
    }

    @Transactional
    public void changeMeetHost(Long userId, Long meetId, HostChangeRequest request) {
        meetHostTransferService.changeMeetHost(userId, meetId, request);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<MeetClientResponse> getHostedMeet(Long userId, CursorPageRequest request) {
        return meetHostTransferService.getHostedMeet(userId, request);
    }

    @Transactional
    public void removeMeet(Long userId, Long meetId) {
        reader.findUser(userId);
        var meet = reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new BadRequestException(NOT_MEMBER);
        }

        if (meet.matchHost(userId)) {
            meetRemoveService.removeMeetAsCreator(meet, userId);

            MeetSoftDeletedEvent deletedEvent = MeetSoftDeletedEvent.builder()
                    .meetId(meet.getId())
                    .meetDeletedBy(userId)
                    .build();

            outboxService.save(MEET_SOFT_DELETED, MEET, meet.getId(), deletedEvent);

            return;
        }

        meetRemoveService.removeMeetAsMember(meetId, userId);

        MeetLeftEvent leftEvent = MeetLeftEvent.builder()
                .meetId(meetId)
                .leaveMemberId(userId)
                .build();

        outboxService.save(MEET_LEFT, MEET, meetId, leftEvent);
    }

    @Transactional
    public String createInvite(Long userId, Long meetId) {

        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new BadRequestException(NOT_MEMBER);
        }

        MeetInvite meetInvite =
                meetInviteRepository.save(
                        MeetInvite.builder()
                                .meetId(meetId)
                                .build()
                );

        return meetInvite.getInviteUrl(inviteUrl);
    }

    @Transactional
    public MeetClientResponse meetJoinMember(Long userId, String meetCode) {
        User user = reader.findUser(userId);

        MeetInvite inviteMeet = meetInviteRepository.findByInviteCodeMeet(meetCode)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_INVITE));

        Meet meet = reader.findMeet(inviteMeet.getMeetId());

        if (meetMemberRepository.existsByMeetIdAndUserId(meet.getId(), userId)) {
            throw new BadRequestException(CURRENT_MEMBER);
        }

        meetMemberRepository.save(
                MeetMember.builder()
                        .meetId(meet.getId())
                        .userId(userId)
                        .nickName(user.getNickname())
                        .hostId(meet.getHostId())
                        .build()
        );

        MeetJoinedEvent joinedEvent = MeetJoinedEvent.builder()
                .meetId(meet.getId())
                .newMemberId(userId)
                .build();

        outboxService.save(MEET_JOINED, MEET, meet.getId(), joinedEvent);

        Integer memberCount = meetRepositorySupport.countMeetMember(meet.getId());

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional(readOnly = true)
    public void inviteMeetInfo(String code, Model model) {
        MeetInvite inviteMeet = meetInviteRepository.findByInviteCodeMeet(code)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_INVITE));

        Optional<Meet> meet = meetRepository.findByIdAndStatus(inviteMeet.getMeetId(), Status.ACTIVE);

        if (meet.isEmpty()) {
            model.addAttribute("meetId", null);
            model.addAttribute("meetName", "없습니다");
            model.addAttribute("meetImage", "https://www.urbanbrush.net/web/wp-content/uploads/edd/2023/03/urban-20230310112234917676.jpg");

            return;
        }

        model.addAttribute("meetId", inviteMeet.getInviteCode());
        model.addAttribute("meetName", meet.get().getName());
        model.addAttribute("meetImage", meet.get().getMeetImage());
    }
}
