package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record PlanCreateNotifyEvent(
        String meetName,
        Long planId,
        String planName
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정등록 \uD83D\uDCC6",
                planName + "에 참여해보세요!"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("planId", planId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_CREATE;
    }
}
