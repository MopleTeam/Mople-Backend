package com.mople.notification.utils;

import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.FirebaseToken;
import com.mople.global.enums.PushTopic;
import com.mople.notification.reader.PushTokenReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotifySendRequestFactory {
    private final PushTokenReader tokenReader;

    public NotifySendRequest buildForTargets(List<Long> userIds, PushTopic pushTopic) {
        List<FirebaseToken> pushTokens = tokenReader.findTokensWithPushTopic(userIds, pushTopic);

        return new NotifySendRequest(
                userIds,
                pushTokens
        );
    }
}
