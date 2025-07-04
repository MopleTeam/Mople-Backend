package com.groupMeeting.dto.request.meet.plan;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanUpdateRequest(
    Long planId,
    String name,
    @NotNull LocalDateTime planTime,
    String planAddress,
    String title,
    BigDecimal lot,
    BigDecimal lat,
    String weatherAddress
){}
