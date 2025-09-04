package com.mople.global.event.handler.notify.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifyHandler implements NotifyEventHandler<PlanUpdateNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<PlanUpdateNotifyEvent> getHandledType() {
        return PlanUpdateNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanUpdateNotifyEvent event) {
        return requestFactory.buildForTargets(event.getTargetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(PlanUpdateNotifyEvent event, List<Long> userIds) {
        return userIds.stream()
                .map(userId ->
                        Notification.builder()
                                .type(event.notifyType())
                                .meetId(event.getMeetId())
                                .planId(event.getPlanId())
                                .payload(event.payload())
                                .userId(userId)
                                .build()
                )
                .toList();
    }
}
