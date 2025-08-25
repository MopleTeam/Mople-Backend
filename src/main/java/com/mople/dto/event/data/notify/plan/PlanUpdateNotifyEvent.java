package com.mople.dto.event.data.notify.plan;

import com.mople.dto.event.data.notify.NotifyEvent;
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
    public String getTitle() {
        return meetName + "의 일정변경";
    }

    @Override
    public String getBody() {
        return planName + " 일정이 변경됐어요";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("planId", planId.toString());
    }
}
