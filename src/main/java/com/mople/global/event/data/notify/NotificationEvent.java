package com.mople.global.event.data.notify;

import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.PushTopic;

import java.util.Map;

public record NotificationEvent(
        PushTopic topic,
        NotificationPayload payload,
        Map<String, String> body
) {
}
