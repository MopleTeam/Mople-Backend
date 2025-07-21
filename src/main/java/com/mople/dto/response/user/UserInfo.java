package com.mople.dto.response.user;

import lombok.Builder;

@Builder
public record UserInfo(
        Long userId,
        String nickname,
        String image
) {
}
