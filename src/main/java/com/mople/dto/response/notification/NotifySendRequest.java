package com.mople.dto.response.notification;

import com.mople.entity.notification.FirebaseToken;

import java.util.List;
import java.util.Map;

public record NotifySendRequest(
        String token,
        Long badgeCount
) {
    public static List<NotifySendRequest> ofRequest(List<FirebaseToken> tokens, Map<Long, Long> badgeMap) {
        return tokens.stream()
                .map(t ->
                        new NotifySendRequest(
                                t.getToken(),
                                badgeMap.getOrDefault(t.getUserId(), 0L)
                        )
                )
                .toList();
    }
}
