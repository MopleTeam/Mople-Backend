package com.mople.meet.schedule;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class PlanReminderScheduleJob {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TaskScheduler taskScheduler;
    private final PlanReminderWorker worker;

    public void scheduleReminder(Long planId, LocalDateTime planTime, Long userId, String meetName) {
        long hour = Math.min(2, Math.max(1, ChronoUnit.HOURS.between(LocalDateTime.now(), planTime)));
        Instant runAt = planTime.minusHours(hour).atZone(KST).toInstant();

        taskScheduler.schedule(
                () -> worker.runPlanReminder(planId, planTime, userId, meetName),
                runAt
        );
    }
}
