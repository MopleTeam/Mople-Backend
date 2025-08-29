package com.mople.global.event.listener.notify.plan;

import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.event.data.notify.plan.PlanCreateNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanCreateNotifyListener {

    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(PlanCreateEvent event) {

        PlanCreateNotifyEvent notifyEvent = PlanCreateNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(event.getMeetName())
                .planId(event.getPlanId())
                .planName(event.getPlanName())
                .planTime(event.getPlanTime())
                .planCreatorId(event.getPlanCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
