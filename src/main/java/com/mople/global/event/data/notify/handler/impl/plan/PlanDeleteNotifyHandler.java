package com.mople.global.event.data.notify.handler.impl.plan;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.plan.PlanDeleteEventData;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_PLAN;
import static com.mople.global.enums.NotifyType.PLAN_DELETE;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifyHandler implements NotifyHandler<PlanDeleteEventData> {

    private final NotifySendRequestFactory requestFactory;
    private final MeetPlanRepository planRepository;

    @Override
    public NotifyType getType() {
        return PLAN_DELETE;
    }

    @Override
    public Class<PlanDeleteEventData> getHandledType() {
        return PlanDeleteEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(PlanDeleteEventData data, NotificationEvent notify) {
        return requestFactory.getPlanPushTokens(data.getPlanDeletedBy(), data.getPlanId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(PlanDeleteEventData data, NotificationEvent notify, List<User> users) {
        deletePlan(data.getPlanId());

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

    private void deletePlan(Long id) {
        MeetPlan plan = planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_PLAN));

        plan.getMeet().removePlan(plan);
        planRepository.delete(plan);
    }
}
