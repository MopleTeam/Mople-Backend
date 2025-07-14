package com.groupMeeting.global.event.data.notify;

import java.time.LocalDateTime;

public record RescheduleNotifyPublisher(
        Long planId,
        LocalDateTime planTime,
        Long userId
) {
}
