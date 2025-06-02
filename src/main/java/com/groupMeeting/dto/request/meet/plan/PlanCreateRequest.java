package com.groupMeeting.dto.request.meet.plan;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PlanCreateRequest(
     Long meetId,
     String name,
     LocalDateTime planTime,
     String planAddress,
     String title,
     BigDecimal lot,
     BigDecimal lat,
     String weatherAddress
){}
