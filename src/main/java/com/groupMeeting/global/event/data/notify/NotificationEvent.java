package com.groupMeeting.global.event.data.notify;

import com.groupMeeting.dto.response.notification.NotificationPayload;
import com.groupMeeting.global.enums.PushTopic;

import java.util.Map;

public record NotificationEvent(
        PushTopic topic,
        NotificationPayload payload,
        Map<String, String> body
) {
}
