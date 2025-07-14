package com.groupMeeting.global.event.listener.notify;

import com.groupMeeting.core.annotation.event.ApplicationEventListener;
import com.groupMeeting.dto.response.notification.NotificationPayload;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.NotifyEventPublisher;
import com.groupMeeting.global.event.data.notify.rescheduleNotifyPublisher;
import com.groupMeeting.meet.schedule.PlanScheduleJob;
import com.groupMeeting.notification.service.NotificationSendService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static com.groupMeeting.global.enums.PushTopic.*;

@Slf4j
@ApplicationEventListener
@RequiredArgsConstructor
public class NotificationEventListener {
    private final NotificationSendService service;
    private final PlanScheduleJob planScheduleJob;
    private final TaskScheduler taskScheduler;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushEventListener(NotifyEventPublisher event) {
        NotificationEvent notify =
                switch (event.type()) {
                    case MEET_NEW_MEMBER ->
                            new NotificationEvent(MEET,
                                    new NotificationPayload(event.data().getTitle(), event.data().getBody()),
                                    event.data().getRoutingKey());

                    case PLAN_CREATE,
                         PLAN_UPDATE,
                         PLAN_DELETE,
                         PLAN_REMIND,
                         REVIEW_REMIND,
                         REVIEW_UPDATE ->
                            new NotificationEvent(PLAN,
                                    new NotificationPayload(event.data().getTitle(), event.data().getBody()),
                                    event.data().getRoutingKey());

                    case COMMENT_REPLY ->
                            new NotificationEvent(
                                    REPLY,
                                    new NotificationPayload(event.data().getTitle(), event.data().getBody()),
                                    event.data().getRoutingKey()
                            );

                    case COMMENT_MENTION ->
                            new NotificationEvent(
                                    MENTION,
                                    new NotificationPayload(event.data().getTitle(), event.data().getBody()),
                                    event.data().getRoutingKey()
                            );
                };

        service.sendMultiNotification(notify, event.type(), event.data());
    }

    @EventListener
    public void reScheduleNotificationEvent(rescheduleNotifyPublisher publisher) {
        LocalDateTime now = LocalDateTime.now();

        log.info("reScheduleEvent time = {}, planId = {}", publisher.planTime(), publisher.planId());

        if (publisher.planTime().isAfter(now.plusHours(1))) {
            long hour = publisher.planTime().until(now, ChronoUnit.HOURS) == 1 ? 1 : 2;

            taskScheduler.schedule(
                    () -> planScheduleJob.planRemindSchedule(publisher.planId(), publisher.planTime(), publisher.userId()),
                    publisher.planTime().minusHours(hour).atZone(ZoneId.systemDefault()).toInstant()
            );
        }
    }
}
