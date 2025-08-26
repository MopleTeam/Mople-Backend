package com.mople.global.event.listener.domain.plan;

import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.meet.schedule.PlanReminderScheduleJob;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PlanCreateReminderListener {

    private final PlanReminderScheduleJob planScheduleJob;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushEventListener(PlanCreateEvent event) {
        LocalDateTime now = LocalDateTime.now();

        if (!event.planTime().isAfter(now.plusHours(1))) return;

        planScheduleJob.scheduleReminder(
                event.planId(),
                event.planTime(),
                event.planCreatorId(),
                event.meetName()
        );
    }
}
