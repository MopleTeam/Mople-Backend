package com.mople.dto.response.meet;

import com.mople.entity.meet.Meet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MeetInfoResponse(
        Long meetId,
        Long version,
        String meetName,
        String meetImage,
        Long creatorId,
        Long meetStartDate,
        int memberCount
) {
    public MeetInfoResponse(Meet meet, int memberCount) {
        this(
                meet.getId(),
                meet.getVersion(),
                meet.getName(),
                meet.getMeetImage(),
                meet.getCreatorId(),
                meet.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS),
                memberCount
        );
    }
}
