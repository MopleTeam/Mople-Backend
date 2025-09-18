package com.mople.meet.service;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.domain.meet.MeetImageChangedEvent;
import com.mople.dto.event.data.domain.meet.MeetJoinedEvent;
import com.mople.dto.event.data.domain.meet.MeetLeftEvent;
import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.dto.request.meet.MeetCreateRequest;
import com.mople.dto.request.meet.MeetUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.*;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.entity.meet.*;
import com.mople.meet.repository.*;

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

@Service
public class MeetService {

    private static final int MEET_CURSOR_FIELD_COUNT = 1;
    private static final int MEET_MEMBER_CURSOR_FIELD_COUNT = 2;

    private final MeetRepository meetRepository;
    private final MeetMemberRepository meetMemberRepository;
    private final MeetInviteRepository meetInviteRepository;
    private final MeetRepositorySupport meetRepositorySupport;
    private final MeetMemberRepositorySupport meetMemberRepositorySupport;
    private final UserRepository userRepository;
    private final OutboxService outboxService;
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
        this.reader = reader;
        this.inviteUrl = inviteUrl;
    }

    @Transactional
    public MeetClientResponse createMeet(Long creatorId, MeetCreateRequest request) {
        var user = reader.findUser(creatorId);

        Meet meet =
                meetRepository.save(
                        Meet.builder()
                                .creatorId(user.getId())
                                .meetImage(request.image())
                                .name(request.name())
                                .build()
                );

        meetMemberRepository.save(
                MeetMember.builder()
                        .meetId(meet.getId())
                        .userId(user.getId())
                        .build()
        );

        Integer memberCount = meetRepositorySupport.countMeetMember(meet.getId());

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional
    public MeetClientResponse updateMeet(Long creatorId, Long meetId, MeetUpdateRequest request) {
        reader.findUser(creatorId);
        var meet = reader.findMeet(meetId);

        if (!meet.matchCreator(creatorId)) {
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

        Long hostId = meet.getCreatorId();
        int size = request.getSafeSize();
        List<MeetMember> meetMembers = getMeetMembers(meet.getId(), hostId, request.cursor(), size);

        Integer memberCount = meetRepositorySupport.countMeetMember(meetId);

        return FlatCursorPageResponse.of(
                memberCount,
                buildMemberCursorPage(size, meetMembers, hostId)
        );
    }

    private List<MeetMember> getMeetMembers(Long meetId, Long hostId, String encodedCursor, int size) {

        MemberCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

            String cursorNickname = decodeParts[0];
            Long cursorId = Long.valueOf(decodeParts[1]);
            validateCursor(cursorNickname, cursorId);

            cursor = new MemberCursor(cursorNickname, cursorId, hostId);
        }

        return meetMemberRepositorySupport.findMemberPage(meetId, hostId, cursor, size);
    }

    private CursorPageResponse<UserRoleClientResponse> buildMemberCursorPage(int size, List<MeetMember> members, Long hostId) {
        List<Long> userIds = members.stream()
                .map(MeetMember::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

        return buildCursorPage(
                members,
                size,
                m -> {
                    User user = reader.findUser(m.getUserId());
                    return new String[]{
                            user.getNickname(),
                            m.getId().toString()
                    };
                },
                list -> ofMembers(list, userInfoById, hostId)
        );
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (meetMemberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public void removeMeet(Long userId, Long meetId) {
        reader.findUser(userId);
        var meet = reader.findMeet(meetId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new BadRequestException(NOT_MEMBER);
        }

        if (meet.matchCreator(userId)) {

            meet.softDelete(userId);

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

            MeetSoftDeletedEvent deletedEvent = MeetSoftDeletedEvent.builder()
                    .meetId(meetId)
                    .meetDeletedBy(userId)
                    .build();

            outboxService.save(MEET_SOFT_DELETED, MEET, meetId, deletedEvent);

            return;
        }

        meetMemberRepository.deleteByMeetIdAndUserId(meetId, userId);

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
        reader.findUser(userId);

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
