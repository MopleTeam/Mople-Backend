package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.*;
import com.groupMeeting.dto.client.MeetClientResponse;
import com.groupMeeting.dto.event.data.meet.MeetJoinEventData;
import com.groupMeeting.dto.request.meet.MeetCreateRequest;
import com.groupMeeting.dto.request.meet.MeetUpdateRequest;
import com.groupMeeting.dto.response.meet.MeetMemberResponse;
import com.groupMeeting.dto.response.meet.*;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.global.event.data.notify.NotifyEventPublisher;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.impl.MeetRepositorySupport;
import com.groupMeeting.entity.meet.*;
import com.groupMeeting.meet.repository.*;

import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.*;

import static com.groupMeeting.dto.client.MeetClientResponse.*;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
public class MeetService {
    private final MeetRepository meetRepository;
    private final MeetMemberRepository meetMemberRepository;
    private final MeetInviteRepository meetInviteRepository;
    private final MeetRepositorySupport meetRepositorySupport;
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
    public List<MeetClientResponse> getUserMeetList(Long userId) {
        return ofListMeets(meetRepositorySupport.findMeetList(userId));
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
    public MeetMemberResponse meetMemberList(Long meetId, Long userId) {
        var meet = meetRepository.findMeetAll(meetId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEET)
        );

        return new MeetMemberResponse(meet);
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
