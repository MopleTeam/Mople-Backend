package com.mople.dto.response.user;

public record UserInfoResponse (
        Long userId,
        String nickname,
        String image,
        int badgeCount
) {}