package com.mople.dto.response.meet.plan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanViewResponse(
        Long planId,
        Long meetId,
        String meetName,
        String meetImage,
        String planName,
        Long creatorId,
        Integer planMemberCount,
        LocalDateTime planTime,
        String planAddress,
        String title,
        BigDecimal lat,
        BigDecimal lot,
        String weatherIcon,
        String weatherAddress,
        Double temperature,
        Double pop
) {
}
