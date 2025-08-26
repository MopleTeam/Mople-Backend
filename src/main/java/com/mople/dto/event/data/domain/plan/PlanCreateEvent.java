package com.mople.dto.event.data.domain.plan;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PlanCreateEvent(
        Long meetId,
        String meetName,
        Long planId,
        String planName,
        LocalDateTime planTime,
        BigDecimal lat,
        BigDecimal lot,
        Long planCreatorId
) {
}
