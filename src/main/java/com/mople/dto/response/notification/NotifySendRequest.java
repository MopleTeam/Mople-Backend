package com.mople.dto.response.notification;

import com.mople.entity.notification.FirebaseToken;

import java.util.List;

public record NotifySendRequest(
        List<Long> userIds,
        List<FirebaseToken> tokens
) {
    public Long findUserByToken(FirebaseToken token) {
        return userIds
                .stream()
                .filter(userId -> userId.equals(token.getUserId()))
                .findFirst()
                .orElse(null);
    }

    public String validToken(FirebaseToken token) {
        return token.getToken();
    }
}
