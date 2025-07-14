package com.groupMeeting.dto.event.data.plan.impl;

import com.groupMeeting.dto.event.data.plan.PlanEventData;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class PlanCreateEventData implements PlanEventData {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final LocalDateTime planTime;
    private final Long creatorId;

    @Override
    public String getTitle() {
        return meetName + "의 일정등록 \uD83D\uDCC6";
    }

    @Override
    public String getBody() {
        return planName + "에 참여해보세요!";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("planId", planId.toString());
    }

    @Override
    public Long triggeredBy() {
        return creatorId;
    }
}
