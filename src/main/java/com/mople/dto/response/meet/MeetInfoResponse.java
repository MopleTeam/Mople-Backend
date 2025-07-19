package com.mople.dto.response.meet;

import com.mople.entity.meet.Meet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record MeetInfoResponse(
        Long meetId,
        String meetName,
        String meetImage,
        Long creatorId,
        Long meetStartDate,
        int memberCount
) {
    public MeetInfoResponse(Meet meet) {
        this(
                meet.getId(),
                meet.getName(),
                meet.getMeetImage(),
                meet.getCreator().getId(),
                meet.getCreatedAt().until(LocalDateTime.now(), ChronoUnit.DAYS),
                meet.getMembers().size()
        );
    }
}
