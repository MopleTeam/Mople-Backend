package com.mople.meet.schedule;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.domain.review.ReviewCreatedEvent;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.REVIEW_CREATED;

@Service
@RequiredArgsConstructor
public class PlanTransitionService {

    private final MeetPlanRepository planRepository;
    private final PlanParticipantRepository participantRepository;
    private final PlanReviewRepository reviewRepository;
    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void transitionPlanByOne(Long planId) {

        MeetPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_PLAN));

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

        ReviewCreatedEvent createdEvent = ReviewCreatedEvent.builder()
                .reviewId(review.getId())
                .build();

        outboxService.save(REVIEW_CREATED, REVIEW, review.getId(), createdEvent);
    }
}
