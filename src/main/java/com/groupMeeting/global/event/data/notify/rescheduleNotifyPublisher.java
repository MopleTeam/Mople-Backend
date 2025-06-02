package com.groupMeeting.global.event.data.notify;

import java.time.LocalDateTime;

public record rescheduleNotifyPublisher(
        Long planId,
        LocalDateTime planTime,
        Long userId
) {
}
