package com.mople.global.event.listener.notify.plan;

import com.mople.dto.event.data.domain.plan.PlanUpdateEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifyListener {

    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(PlanUpdateEvent event) {

        PlanUpdateNotifyEvent notifyEvent = PlanUpdateNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(event.getMeetName())
                .planId(event.getPlanId())
                .planName(event.getPlanName())
                .planUpdatedBy(event.getPlanUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
