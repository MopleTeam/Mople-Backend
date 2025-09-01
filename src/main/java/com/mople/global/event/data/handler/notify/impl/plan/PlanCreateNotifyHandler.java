package com.mople.global.event.data.handler.notify.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanCreateNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;

@Component
@RequiredArgsConstructor
public class PlanCreateNotifyHandler implements NotifyEventHandler<PlanCreateNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<PlanCreateNotifyEvent> getHandledType() {
        return PlanCreateNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanCreateNotifyEvent event) {
        return requestFactory.getMeetPushTokens(event.getPlanCreatorId(), event.getMeetId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(PlanCreateNotifyEvent event, List<User> users) {
        return users.stream()
                .map(u -> Notification.builder()
                        .type(event.notifyType())
                        .action(COMPLETE)
                        .meetId(event.getMeetId())
                        .planId(event.getPlanId())
                        .payload(event.payload())
                        .userId(u.getId())
                        .build())
                .toList();
    }
}
