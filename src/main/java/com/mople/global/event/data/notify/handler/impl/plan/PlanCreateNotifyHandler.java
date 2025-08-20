package com.mople.global.event.data.notify.handler.impl.plan;

import com.mople.dto.event.data.plan.PlanCreateEventData;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.NotifyType.PLAN_CREATE;

@Component
@RequiredArgsConstructor
public class PlanCreateNotifyHandler implements NotifyHandler<PlanCreateEventData> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return PLAN_CREATE;
    }

    @Override
    public Class<PlanCreateEventData> getHandledType() {
        return PlanCreateEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanCreateEventData data, NotificationEvent notify) {
        return requestFactory.getMeetPushTokens(data.getPlanCreatorId(), data.getMeetId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(PlanCreateEventData data, NotificationEvent notify, List<User> users) {
        return users.stream()
                .map(u -> Notification.builder()
                        .type(getType())
                        .action(COMPLETE)
                        .meetId(data.getMeetId())
                        .planId(data.getPlanId())
                        .payload(notify.payload())
                        .user(u)
                        .build())
                .toList();
    }
}
