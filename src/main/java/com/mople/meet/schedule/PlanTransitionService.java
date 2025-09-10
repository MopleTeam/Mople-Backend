package com.mople.meet.schedule;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanTransitionedEvent;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.*;

@Service
@RequiredArgsConstructor
public class PlanTransitionService {

    private static final long SYSTEM_USER_ID = 0L;

    private final MeetPlanRepository planRepository;
    private final PlanParticipantRepository participantRepository;
    private final PlanReviewRepository reviewRepository;
    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.MANDATORY)
    public void transitionPlanByOne(Long planId) {
        MeetPlan plan = planRepository.findByIdAndStatus(planId, Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

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

        participantRepository.updateReviewId(plan.getId(), review.getId());

        planRepository.softDelete(Status.DELETED, plan.getId(), SYSTEM_USER_ID, LocalDateTime.now());

        PlanTransitionedEvent transitionedEvent = PlanTransitionedEvent.builder()
                .planId(review.getPlanId())
                .reviewId(review.getId())
                .build();

        outboxService.save(PLAN_TRANSITIONED, PLAN, review.getPlanId(), transitionedEvent);
    }
}
