package com.mople.dto.request.notification.topic;

import com.mople.global.enums.PushTopic;

import java.util.List;

public record PushTopicRequest(
        List<PushTopic> topics
) {
}
