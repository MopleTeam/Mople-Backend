package com.groupMeeting.dto.event.data.plan.impl;

import com.groupMeeting.dto.event.data.plan.PlanEventData;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PlanDeleteEventData implements PlanEventData {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final Long deletedBy;

    @Override
    public String getTitle() {
        return meetName + "의 일정취소";
    }

    @Override
    public String getBody() {
        return planName + " 일정이 취소됐어요";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("meetId", meetId.toString());
    }

    @Override
    public Long triggeredBy() {
        return deletedBy;
    }
}
