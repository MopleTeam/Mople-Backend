package com.groupMeeting.dto.request.notification.topic;

import com.groupMeeting.global.enums.PushTopic;

import java.util.List;

public record PushTopicRequest(
        List<PushTopic> topics
) {
}
