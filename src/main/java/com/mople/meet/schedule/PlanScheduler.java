package com.mople.meet.schedule;

import com.mople.dto.event.data.domain.plan.PlanTransitionRequestedEvent;
import com.mople.global.enums.Status;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.PLAN_TRANSITION_REQUESTED;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class PlanScheduler {

    private final MeetPlanRepository meetPlanRepository;
    private final OutboxService outboxService;

    @Scheduled(cron = "${cron.plan.transition}", zone = "Asia/Seoul")
    public void previousPlanReviewChangeSchedule() {
        List<Long> previousPlanIds = meetPlanRepository.findPreviousPlanAll(LocalDateTime.now(), Status.ACTIVE);

        chunk(previousPlanIds, ids ->
            ids.forEach(id -> {
                PlanTransitionRequestedEvent requestedEvent = PlanTransitionRequestedEvent.builder()
                        .planId(id)
                        .build();

                outboxService.save(PLAN_TRANSITION_REQUESTED, PLAN, id, requestedEvent);
            })
        );
    }
}