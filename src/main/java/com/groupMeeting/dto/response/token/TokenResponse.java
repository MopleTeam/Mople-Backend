package com.groupMeeting.dto.response.token;

import lombok.Builder;

@Builder
public record TokenResponse (
        String accessToken,
        String refreshToken
) {
}
