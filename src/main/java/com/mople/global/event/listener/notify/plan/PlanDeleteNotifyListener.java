package com.mople.global.event.listener.notify.plan;

import com.mople.dto.event.data.domain.plan.PlanDeleteEvent;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifyListener {

    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(PlanDeleteEvent event) {

        PlanDeleteNotifyEvent notifyEvent = PlanDeleteNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(event.getMeetName())
                .planId(event.getPlanId())
                .planName(event.getPlanName())
                .planDeletedBy(event.getPlanDeletedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
