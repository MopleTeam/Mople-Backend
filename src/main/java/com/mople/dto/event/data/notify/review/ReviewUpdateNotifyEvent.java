package com.mople.dto.event.data.notify.review;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class ReviewUpdateNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long reviewId;
    private final String reviewName;
    private final Long reviewUpdatedBy;

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 일정은 어떠셨나요?",
                reviewName + "의 사진을 확인해보세요"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("reviewId", reviewId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.REVIEW_UPDATE;
    }
}
