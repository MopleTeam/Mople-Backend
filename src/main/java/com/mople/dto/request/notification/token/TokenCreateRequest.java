package com.mople.dto.request.notification.token;

public record TokenCreateRequest(
        String token,
        boolean subscribe
) {
}
