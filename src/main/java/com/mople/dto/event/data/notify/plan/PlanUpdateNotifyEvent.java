package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PlanUpdateNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final Long planUpdatedBy;

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정변경",
                planName + " 일정이 변경됐어요"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("planId", planId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_UPDATE;
    }
}
