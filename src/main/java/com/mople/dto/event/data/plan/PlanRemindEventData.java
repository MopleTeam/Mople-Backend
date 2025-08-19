package com.mople.dto.event.data.plan;

import com.mople.dto.event.data.EventData;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class PlanRemindEventData implements EventData {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final LocalDateTime planTime;
    private final Long planCreatorId;
    private final Double temperature;
    private final String iconImage;

    @Override
    public String getTitle() {
        return meetName + "의 일정 알림 ⏰";
    }

    @Override
    public String getBody() {
        return planName + " 곧 시작돼요!";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("planId", planId.toString());
    }
}
