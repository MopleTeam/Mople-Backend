package com.mople.dto.response.user;

import com.mople.entity.user.User;
import lombok.Builder;

@Builder
public record UserInfo(
        Long userId,
        String nickname,
        String image
) {
    public static UserInfo from(User user) {
        return UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .build();
    }
}
