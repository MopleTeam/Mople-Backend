package com.mople.dto.response.meet.plan;

import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlanListResponse(
        Long planId,
        Long version,
        Long meetId,
        String meetName,
        String meetImage,
        String planName,
        Integer planMemberCount,
        LocalDateTime planTime,
        String planAddress,
        String title,
        Long creatorId,
        String weatherIcon,
        String weatherAddress,
        Double temperature,
        Double pop,
        boolean participant
) {
    public PlanListResponse(
            Meet meet,
            MeetPlan plan,
            Integer planMemberCount,
            boolean isParticipant
    ) {
        this(
                plan.getId(),
                plan.getVersion(),
                meet.getId(),
                meet.getName(),
                meet.getMeetImage(),
                plan.getName(),
                planMemberCount,
                plan.getPlanTime(),
                plan.getAddress(),
                plan.getTitle(),
                plan.getCreatorId(),
                plan.getWeatherIcon(),
                plan.getWeatherAddress(),
                plan.getTemperature(),
                plan.getPop(),
                isParticipant
        );
    }
}
