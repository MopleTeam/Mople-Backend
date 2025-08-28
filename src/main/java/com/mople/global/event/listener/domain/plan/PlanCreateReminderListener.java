package com.mople.global.event.listener.domain.plan;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static com.mople.global.enums.AggregateType.PLAN;
import static com.mople.global.enums.EventTypeNames.PLAN_REMIND;

@Component
@RequiredArgsConstructor
public class PlanCreateReminderListener {

    private final MeetPlanRepository planRepository;
    private final OutboxService outboxService;

    @EventListener
    public void pushEventListener(PlanCreateEvent event) {
        MeetPlan plan = planRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        LocalDateTime now = LocalDateTime.now();
        if (!plan.getPlanTime().isAfter(now.plusHours(1))) return;

        long hour = now.until(plan.getPlanTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;
        LocalDateTime runAt = plan.getPlanTime().minusHours(hour).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override public void afterCommit() {
                    publishPlanRemindEvent(event, runAt);
                }
            });
        } else {
            publishPlanRemindEvent(event, runAt);
        }
    }

    private void publishPlanRemindEvent(PlanCreateEvent event, LocalDateTime runAt) {
        PlanRemindEvent remindEvent = PlanRemindEvent.builder()
                .planId(event.getPlanId())
                .build();

        outboxService.saveWithRunAt(PLAN_REMIND, PLAN, event.getPlanId(), runAt, remindEvent);
    }
}
