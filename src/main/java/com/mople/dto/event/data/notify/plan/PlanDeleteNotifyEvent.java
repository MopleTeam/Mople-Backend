package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record PlanDeleteNotifyEvent(
        Long meetId,
        String meetName,
        String planName
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정취소",
                planName + " 일정이 취소됐어요"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("meetId", meetId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_DELETE;
    }
}
