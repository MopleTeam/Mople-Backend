package com.mople.meet.schedule;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.domain.review.ReviewCreateEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.global.enums.AggregateType.REVIEW;
import static com.mople.global.enums.EventTypeNames.REVIEW_CREATE;

@Service
@RequiredArgsConstructor
public class PlanTransitionService {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final PlanParticipantRepository participantRepository;
    private final PlanReviewRepository reviewRepository;
    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transitionPlanByOne(Long planId) {

        MeetPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanReview review = reviewRepository.save(
                PlanReview.builder()
                        .planId(plan.getId())
                        .name(plan.getName())
                        .planTime(plan.getPlanTime())
                        .address(plan.getAddress())
                        .title(plan.getTitle())
                        .latitude(plan.getLatitude())
                        .longitude(plan.getLongitude())
                        .weatherIcon(plan.getWeatherIcon())
                        .weatherAddress(plan.getWeatherAddress())
                        .temperature(plan.getTemperature())
                        .pop(plan.getPop())
                        .creatorId(plan.getCreatorId())
                        .meetId(plan.getMeetId())
                        .build()
        );

        List<PlanParticipant> participants = participantRepository.findParticipantsByPlanId(plan.getId());
        participants.forEach(pp -> pp.updateReview(review.getId()));

        planRepository.delete(plan);

        ReviewCreateEvent event = ReviewCreateEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(review.getId())
                .reviewName(review.getName())
                .reviewCreatorId(review.getCreatorId())
                .isUpload(review.getUpload())
                .build();

        outboxService.save(REVIEW_CREATE, REVIEW, review.getId(), event);
    }
}
