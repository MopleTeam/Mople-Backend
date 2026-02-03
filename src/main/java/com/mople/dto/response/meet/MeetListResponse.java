package com.mople.dto.response.meet;

import java.time.LocalDateTime;

public record MeetListResponse(
        Long meetId,
        Long version,
        String meetName,
        String meetImage,
        Long hostId,
        Integer memberCount,
        LocalDateTime lastPlanDays
) {}
