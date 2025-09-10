package com.mople.dto.response.notification;

import lombok.Builder;

@Builder
public record NotificationSnapshot(
        NotificationPayload payload,
        Long meetId,
        Long planId,
        Long reviewId
) {
}
