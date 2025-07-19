package com.mople.dto.response.meet.plan;

import java.time.LocalDateTime;

public record PlanListResponse(
        Long planId,
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
}
