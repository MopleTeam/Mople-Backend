package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class PlanCreateNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final List<Long> targetIds;

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
    public List<Long> targetIds() {
        return this.targetIds;
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.PLAN_CREATE;
    }
}
