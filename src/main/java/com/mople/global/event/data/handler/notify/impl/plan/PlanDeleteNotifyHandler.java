package com.mople.global.event.data.handler.notify.impl.plan;

import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.handler.notify.NotifyEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifyHandler implements NotifyEventHandler<PlanDeleteNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;

    @Override
    public Class<PlanDeleteNotifyEvent> getHandledType() {
        return PlanDeleteNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanDeleteNotifyEvent event) {
        return requestFactory.getPlanPushTokens(event.getPlanDeletedBy(), event.getPlanId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(PlanDeleteNotifyEvent event, List<User> users) {
        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(event.notifyType())
                                .action(COMPLETE)
                                .meetId(event.getMeetId())
                                .planId(event.getPlanId())
                                .payload(event.payload())
                                .userId(u.getId())
                                .build()
                )
                .toList();
    }
}
