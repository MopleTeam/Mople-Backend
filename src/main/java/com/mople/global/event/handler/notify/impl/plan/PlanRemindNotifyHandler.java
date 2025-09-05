package com.mople.global.event.handler.notify.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyHandler implements NotifyEventHandler<PlanRemindNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<PlanRemindNotifyEvent> getHandledType() {
        return PlanRemindNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanRemindNotifyEvent event) {
        return requestFactory.buildForTargets(event.targetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(PlanRemindNotifyEvent event, List<Long> userIds) {
        return userIds.stream()
                .map(userId ->
                        Notification.builder()
                                .type(event.notifyType())
                                .meetId(event.meetId())
                                .planId(event.planId())
                                .payload(event.payload())
                                .userId(userId)
                                .build()
                )
                .toList();
    }
}
