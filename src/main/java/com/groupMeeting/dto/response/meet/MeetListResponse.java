package com.groupMeeting.dto.response.meet;

import java.time.LocalDateTime;

public record MeetListResponse(
        Long meetId,
        String meetName,
        String meetImage,
        int memberCount,
        LocalDateTime lastPlanDays
) {}
