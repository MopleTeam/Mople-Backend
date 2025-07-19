package com.mople.global.event.data.notify.handler.impl.plan;

import com.mople.dto.event.data.plan.PlanRemindEventData;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.NotifyType.PLAN_REMIND;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyHandler implements NotifyHandler<PlanRemindEventData> {

    private final NotifySendRequestFactory requestFactory;
    private final NotificationRepository notificationRepository;

    @Override
    public NotifyType getType() {
        return PLAN_REMIND;
    }

    @Override
    public Class<PlanRemindEventData> getHandledType() {
        return PlanRemindEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanRemindEventData data, NotificationEvent notify) {
        return requestFactory.getPlanRemindToken(data.getPlanId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(PlanRemindEventData data, NotificationEvent notify, List<User> users) {
        List<Notification> existing =
                notificationRepository.findPlanRemindNotification(data.getPlanId(), Action.PENDING);

        existing.forEach(n -> n.updateNotification(notify, getType()));
        return existing;
    }
}
