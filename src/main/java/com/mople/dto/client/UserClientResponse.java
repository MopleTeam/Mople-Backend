package com.mople.dto.client;

import lombok.Builder;

@Builder
public record UserClientResponse(
        Long userId,
        String nickname,
        String image,
        boolean isExistBadgeCount
) {
}
