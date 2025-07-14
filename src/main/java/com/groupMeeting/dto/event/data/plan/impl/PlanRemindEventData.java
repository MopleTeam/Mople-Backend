package com.groupMeeting.dto.event.data.plan.impl;

import com.groupMeeting.dto.event.data.plan.PlanEventData;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class PlanRemindEventData implements PlanEventData {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final LocalDateTime planTime;
    private final Long creatorId;
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
        return Map.of();
    }

    @Override
    public Long triggeredBy() {
        return creatorId;
    }
}
