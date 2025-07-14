package com.groupMeeting.global.event.data.notify.handler.impl.plan;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.event.data.plan.PlanDeleteEventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandler;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.groupMeeting.global.enums.Action.COMPLETE;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_PLAN;
import static com.groupMeeting.global.enums.NotifyType.PLAN_DELETE;

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
        return requestFactory.getPlanPushToken(data.getDeletedBy(), data.getPlanId(), notify.topic());
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
