package com.mople.dto.response.meet.plan;

import java.time.LocalDateTime;

public record PlanInfoResponse(
        Long planId,
        String planName,
        LocalDateTime planTime,
        int participantCount,
        String weatherIcon,
        Double temperature,
        int pop
){}