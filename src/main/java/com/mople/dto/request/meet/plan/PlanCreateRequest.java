package com.mople.dto.request.meet.plan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanCreateRequest(
     Long meetId,
     String name,
     LocalDateTime planTime,
     String planAddress,
     String title,
     String description,
     BigDecimal lot,
     BigDecimal lat,
     String weatherAddress
){}
