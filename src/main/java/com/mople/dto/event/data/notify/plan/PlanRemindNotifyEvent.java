package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record PlanRemindNotifyEvent(
        String meetName,
        Long planId,
        String planName
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정 알림 ⏰",
                planName + " 곧 시작돼요!"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("planId", planId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_REMIND;
    }
}
