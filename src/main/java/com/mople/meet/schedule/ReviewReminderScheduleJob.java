package com.mople.meet.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.*;

@Component
@RequiredArgsConstructor
public class ReviewReminderScheduleJob {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final TaskScheduler taskScheduler;
    private final ReviewReminderWorker worker;

    public void scheduleReminder(Long reviewId, String meetName) {
        taskScheduler.schedule(
                () -> worker.runReviewReminder(reviewId, meetName),
                LocalDateTime
                        .of(LocalDate.now(), LocalTime.of(12, 0, 0))
                        .atZone(KST)
                        .toInstant()
        );
    }
}
