package com.mople.dto.response.meet;

import java.time.LocalDateTime;

public record PlanPageResponse(
        Long meetId,
        String meetName,
        String meetImage,
        Long planId,
        String planName,
        Long creatorId,
        LocalDateTime planTime,
        Integer planParticipants,
        String weatherIcon,
        String weatherAddress,
        Double temperature,
        Double pop
//        Long reviewId,
//        String reviewName,
//        LocalDateTime reviewTime,
//        String reviewAddress,
//        String reviewTitle,
//        BigDecimal reviewLat,
//        BigDecimal reviewLot,
//        Integer reviewParticipants
) {
}
