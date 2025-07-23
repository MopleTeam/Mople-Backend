package com.mople.meet.service;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.event.data.meet.MeetJoinEventData;
import com.mople.dto.request.meet.MeetCreateRequest;
import com.mople.dto.request.meet.MeetUpdateRequest;
import com.mople.dto.client.MeetMemberClientResponse;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.*;
import com.mople.dto.response.pagination.CursorPageResponse;
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
                                .creator(user)
                                .name(request.name())
                                .meetImage(request.image())
                                .build()
                );

        MeetMember member =
                meetMemberRepository.save(
                        MeetMember.builder()
                                .user(user)
                                .build()
                );

        meet.addMember(member);

        return ofMeet(new MeetInfoResponse(meet));
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
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return meetRepositorySupport.findMeetFirstPage(userId, size);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_CURSOR_FIELD_COUNT);
        Long cursorId = Long.valueOf(decodeParts[0]);

        validateCursor(cursorId);

        return meetRepositorySupport.findMeetNextPage(userId, cursorId, size);
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
                c -> new String[]{
                        c.meetId().toString()
                },
                MeetClientResponse::ofListMeets
        );
    }

    @Transactional(readOnly = true)
    public MeetClientResponse getMeetDetail(Long meetId, Long userId) {
        var meet = reader.findMeet(meetId);

        if (meet.matchMember(userId)) {
            throw new AuthException(NOT_FOUND_MEMBER);
        }

        return ofMeet(new MeetInfoResponse(meet));
    }

    @Transactional(readOnly = true)
    public MeetMemberClientResponse meetMemberList(Long meetId, Long userId, CursorPageRequest request) {
        reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);
        validateMember(userId, meetId);

        int size = request.getSafeSize();
        List<MeetMember> meetMembers = getMeetMembers(meet.getId(), request.cursor(), size);

        return MeetMemberClientResponse.builder()
                .creatorId(meet.getCreator().getId())
                .members(buildMemberCursorPage(size, meetMembers))
                .build();
    }

    private List<MeetMember> getMeetMembers(Long meetId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return meetMemberRepositorySupport.findMemberFirstPage(meetId, size);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor, MEET_MEMBER_CURSOR_FIELD_COUNT);

        String cursorNickname = decodeParts[0];
        Long cursorId = Long.valueOf(decodeParts[1]);

        validateCursor(cursorNickname, cursorId);

        return meetMemberRepositorySupport.findMemberNextPage(meetId, cursorNickname, cursorId, size);
    }

    private CursorPageResponse<MeetMemberResponse> buildMemberCursorPage(int size, List<MeetMember> members) {
        return buildCursorPage(
                members,
                size,
                c -> new String[]{
                        c.getUser().getNickname(),
                        c.getUser().getId().toString()
                },
                MeetMemberResponse::ofMemberList
        );
    }

    private void validateMember(Long userId, Long meetId) {
        Meet meet = reader.findMeet(meetId);
        if (meet.matchMember(userId)) {
            throw new BadRequestException(NOT_MEMBER);
        }
    }

    private void validateCursor(String cursorNickname, Long cursorId) {
        if (meetMemberRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional
    public void removeMeet(Long meetId, Long userId) {
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
                        MeetJoinEventData.builder()
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

        Meet meet = meetRepository.findMeetById(inviteMeet.getMeetId()).orElse(null);

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
