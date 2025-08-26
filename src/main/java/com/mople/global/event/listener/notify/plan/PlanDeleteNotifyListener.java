package com.mople.global.event.listener.notify.plan;

import com.mople.dto.event.data.domain.plan.PlanDeleteEvent;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifyListener {

    private final NotificationSendService sendService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushEventListener(PlanDeleteEvent event) {

        PlanDeleteNotifyEvent notifyEvent = PlanDeleteNotifyEvent.builder()
                .meetId(event.meetId())
                .meetName(event.meetName())
                .planId(event.planId())
                .planName(event.planName())
                .planDeletedBy(event.planDeletedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
