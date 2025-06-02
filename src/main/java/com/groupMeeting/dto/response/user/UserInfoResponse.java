package com.groupMeeting.dto.response.user;

public record UserInfoResponse (
        Long userId,
        String nickname,
        String image,
        int badgeCount
) {}