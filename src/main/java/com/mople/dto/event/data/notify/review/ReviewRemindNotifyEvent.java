package com.mople.dto.event.data.notify.review;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record ReviewRemindNotifyEvent(
        String meetName,
        Long reviewId,
        String reviewName
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정은 어떠셨나요?",
                reviewName + "의 사진을 공유해보세요"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("reviewId", reviewId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.REVIEW_REMIND;
    }
}
