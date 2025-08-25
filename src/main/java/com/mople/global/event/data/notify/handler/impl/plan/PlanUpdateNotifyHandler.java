package com.mople.global.event.data.notify.handler.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
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
import static com.mople.global.enums.NotifyType.PLAN_UPDATE;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifyHandler implements NotifyHandler<PlanUpdateNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return PLAN_UPDATE;
    }

    @Override
    public Class<PlanUpdateNotifyEvent> getHandledType() {
        return PlanUpdateNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanUpdateNotifyEvent data, NotificationEvent notify) {
        return requestFactory.getPlanPushTokens(data.getPlanUpdatedBy(), data.getPlanId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(PlanUpdateNotifyEvent data, NotificationEvent notify, List<User> users) {
        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(getType())
                                .action(COMPLETE)
                                .meetId(data.getMeetId())
                                .planId(data.getPlanId())
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }
}
