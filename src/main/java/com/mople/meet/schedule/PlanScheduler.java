package com.mople.meet.schedule;

import com.mople.dto.event.data.domain.plan.PlanNoLocationEvent;
import com.mople.dto.event.data.domain.plan.PlanTransitionRequestedEvent;
import com.mople.meet.repository.impl.plan.PlanRepositorySupport;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.PLAN_NO_LOCATION;
import static com.mople.global.enums.event.EventTypeNames.PLAN_TRANSITION_REQUESTED;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class PlanScheduler {

    private final PlanRepositorySupport planRepositorySupport;
    private final OutboxService outboxService;

    @Scheduled(cron = "${cron.plan.transition}", zone = "Asia/Seoul")
    public void previousPlanReviewChangeSchedule() {
        List<Long> previousPlanIds = planRepositorySupport.findPreviousPlanAll();

        chunk(previousPlanIds, ids ->
            ids.forEach(id -> {
                PlanTransitionRequestedEvent requestedEvent = PlanTransitionRequestedEvent.builder()
                        .planId(id)
                        .build();

                outboxService.save(PLAN_TRANSITION_REQUESTED, PLAN, id, requestedEvent);
            })
        );
    }

    @Scheduled(cron = "${cron.plan.no-location}", zone = "Asia/Seoul")
    public void noLocationPlanSchedule() {
        List<Long> noLocationPlanIds = planRepositorySupport.findNoLocationPlanAll();

        chunk(noLocationPlanIds, ids ->
                ids.forEach(id -> {
                    PlanNoLocationEvent requestedEvent = PlanNoLocationEvent.builder()
                            .planId(id)
                            .build();

                    outboxService.save(PLAN_NO_LOCATION, PLAN, id, requestedEvent);
                })
        );
    }
}