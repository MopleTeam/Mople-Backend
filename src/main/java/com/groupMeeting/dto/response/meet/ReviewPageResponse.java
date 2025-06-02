package com.groupMeeting.dto.response.meet;

import java.time.LocalDateTime;

public record ReviewPageResponse(
        Long meetId,
        String meetName,
        String meetImage,
        Long reviewId,
        String reviewName,
        LocalDateTime reviewTime,
        Integer reviewParticipants,
        String weatherIcon,
        String weatherAddress,
        Double temperature,
        Double pop
) {
}
