package com.mople.dto.response.token;

import lombok.Builder;

@Builder
public record TokenResponse (
        String accessToken,
        String refreshToken
) {
}
