package com.groupMeeting.dto.response.user;

import lombok.Builder;

@Builder
public record UserInfo(
        Long userId,
        String nickname,
        String image
) {
}
