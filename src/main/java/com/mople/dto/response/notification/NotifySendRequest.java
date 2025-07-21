package com.mople.dto.response.notification;

import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.user.User;

import java.util.List;

public record NotifySendRequest(
        List<User> users,
        List<FirebaseToken> tokens
) {
    public User findUserByToken(FirebaseToken token) {
        return users
                .stream()
                .filter(user -> user.getId().equals(token.getUserId()))
                .findFirst()
                .orElse(null);
    }

    public String validToken(FirebaseToken token) {

        return token.getToken();
    }
}
