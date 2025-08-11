package com.mople.dto.response.user;

import com.mople.entity.user.User;
import com.mople.global.enums.UserRole;
import lombok.Builder;

@Builder
public record UserInfo(
        Long userId,
        String nickname,
        String image,
        UserRole userRole
) {
    public static UserInfo from(User user, UserRole role) {
        return UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .userRole(role)
                .build();
    }
}
