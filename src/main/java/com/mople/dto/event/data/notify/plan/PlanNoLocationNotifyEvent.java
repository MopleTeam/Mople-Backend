package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

import static com.mople.global.utils.text.HighlightUtils.highlight;

@Builder
public record PlanNoLocationNotifyEvent(
        String meetName,
        Long planId,
        String planName
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 약속장소를 정하지 않았어요! 🙈",
                planName + " 장소 확정이 필요해요",
                highlight(planName) + " 장소 확정이 필요해요"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("planId", planId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_NO_LOCATION;
    }
}
