package com.mople.global.event.listener.notify.plan;

import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.event.data.notify.plan.PlanCreateNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlanCreateNotifyListener {

    private final NotificationSendService sendService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushEventListener(PlanCreateEvent event) {

        PlanCreateNotifyEvent notifyEvent = PlanCreateNotifyEvent.builder()
                .meetId(event.meetId())
                .meetName(event.meetName())
                .planId(event.planId())
                .planName(event.planName())
                .planTime(event.planTime())
                .planCreatorId(event.planCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
