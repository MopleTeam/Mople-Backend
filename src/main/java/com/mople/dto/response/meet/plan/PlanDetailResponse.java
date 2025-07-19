package com.mople.dto.response.meet.plan;

import java.time.LocalDateTime;

public record PlanDetailResponse(
        Long planId,
        Long meetId,
        String meetName,
        String meetImage,
        String planName,
        LocalDateTime planTime,
        String address,
        int participantCount
) {
}
