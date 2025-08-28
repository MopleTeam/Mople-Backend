package com.mople.meet.service;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.notify.meet.MeetJoinNotifyEvent;
import com.mople.dto.request.meet.MeetCreateRequest;
import com.mople.dto.request.meet.MeetUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.*;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.entity.user.User;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.MeetMemberRepositorySupport;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.entity.meet.*;
import com.mople.meet.repository.*;

import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.*;

import static com.mople.dto.client.MeetClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofMembers;
import static com.mople.global.enums.ExceptionReturnCode.*;
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
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReviewRepository reviewRepository;
    private final EntityReader reader;

    private final ApplicationEventPublisher publisher;

    private final String inviteUrl;

    public MeetService(
            MeetRepository meetRepository,
            MeetMemberRepository meetMemberRepository,
            MeetInviteRepository meetInviteRepository,
            MeetRepositorySupport meetRepositorySupport,
            MeetMemberRepositorySupport meetMemberRepositorySupport,
            MeetPlanRepository meetPlanRepository,
            PlanReviewRepository reviewRepository,
            EntityReader reader,
            ApplicationEventPublisher publisher,
            @Value("${mople.url}") String  inviteUrl
    ) {
        this.meetRepository = meetRepository;
        this.meetMemberRepository = meetMemberRepository;
        this.meetInviteRepository = meetInviteRepository;
        this.meetRepositorySupport = meetRepositorySupport;
        this.meetMemberRepositorySupport = meetMemberRepositorySupport;
        this.meetPlanRepository = meetPlanRepository;
        this.reviewRepository = reviewRepository;
        this.reader = reader;
        this.publisher = publisher;
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

        int memberCount = Math.toIntExact(meetMemberRepositorySupport.countMeetMembers(meet.getId()));

        return ofMeet(new MeetInfoResponse(meet, memberCount));
    }

    @Transactional
    public MeetClientResponse updateMeet(Long creatorId, Long meetId, MeetUpdateRequest request) {

        var meet = reader.findMeet(meetId);

        if (!Objects.equals(creatorId, meet.getCreator().getId())) {
            throw new AuthException(NOT_CREATOR);
        }
        meet.updateMeetInfo(request.name(), request.image());

        return ofMeet(new MeetInfoResponse(meet));
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

        int memberCount = Math.toIntExact(meetMemberRepositorySupport.countMeetMembers(meetId));

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

        return FlatCursorPageResponse.of(
                meetMemberRepositorySupport.countMeetMembers(meetId),
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
                list -> ofMembers(list, hostId)
        );
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (meetMemberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public void removeMeet(Long userId, Long meetId) {
        var meet = reader.findMeet(meetId);

        if (meet.matchCreator(userId)) {
            meetRepository.delete(meet);
            return;
        }

        meet.removeMember(userId);

        List<MeetPlan> plans = meet
                .getPlans()
                .stream()
                .filter(p -> p.getCreator().getId().equals(userId))
                .toList();

        if (!plans.isEmpty()) {
            plans.forEach(p -> p.getMeet().removePlan(p));
            meetPlanRepository.deleteAllInBatch(plans);
        }

        reviewRepository.deleteAll(reviewRepository.findReviewByUserId(userId));

        meetMemberRepository.deleteMember(meetId, userId);
    }

    @Transactional
    public String createInvite(Long meetId) {
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
        MeetInvite inviteMeet = meetInviteRepository.findByInviteCodeMeet(meetCode)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_INVITE));

        var meet = reader.findMeet(inviteMeet.getMeetId());
        var user = reader.findUser(userId);

        if (meet.meetMemberSearch(userId)) {
            throw new BadRequestException(CURRENT_MEMBER);
        }

        MeetMember meetMember =
                meetMemberRepository.save(
                        MeetMember
                                .builder()
                                .user(user)
                                .build()
                );

        meet.addMember(meetMember);

        publisher.publishEvent(
                NotifyEventPublisher.meetNewMember(
                        MeetJoinNotifyEvent.builder()
                                .meetId(meet.getId())
                                .meetName(meet.getName())
                                .newMemberId(user.getId())
                                .newMemberNickname(user.getNickname())
                                .build()
                )
        );

        return ofMeet(new MeetInfoResponse(meet));
    }

    @Transactional(readOnly = true)
    public void inviteMeetInfo(String code, Model model) {
        MeetInvite inviteMeet = meetInviteRepository.findByInviteCodeMeet(code)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_INVITE));

        Meet meet = meetRepository.findById(inviteMeet.getMeetId()).orElse(null);

        if (meet == null) {
            model.addAttribute("meetId", null);
            model.addAttribute("meetName", "없습니다");
            model.addAttribute("meetImage", "https://www.urbanbrush.net/web/wp-content/uploads/edd/2023/03/urban-20230310112234917676.jpg");

            return;
        }

        model.addAttribute("meetId", inviteMeet.getInviteCode());
        model.addAttribute("meetName", meet.getName());
        model.addAttribute("meetImage", meet.getMeetImage());
    }
}
