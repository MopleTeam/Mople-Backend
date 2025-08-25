package com.mople.global.event.data.notify.handler.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyHandler implements NotifyHandler<PlanRemindNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;
    private final NotificationRepository notificationRepository;

    @Override
    public Class<PlanRemindNotifyEvent> getHandledType() {
        return PlanRemindNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanRemindNotifyEvent event) {
        return requestFactory.getPlanPushTokensAll(event.getPlanId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(PlanRemindNotifyEvent event, List<User> users) {
        List<Notification> existing =
                notificationRepository.findPlanRemindNotification(event.getPlanId(), Action.PENDING);

        existing.forEach(n -> n.updateNotification(event));
        return existing;
    }
}
