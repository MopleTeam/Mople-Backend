package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class PlanRemindNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final LocalDateTime planTime;
    private final Long planCreatorId;
    private final Double temperature;
    private final String iconImage;

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
