package com.mople.dto.event.data.notify.comment;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record CommentMentionNotifyEvent(
        String meetName,
        Long planId,
        Long reviewId,
        String senderNickname
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 새로운 멘션 👀",
                meetName + "에서 " + senderNickname + "님이 회원님을 멘션했어요!"
        );
    }

    @Override
    public Map<String, String> routing() {
        if (planId != null && reviewId == null) {
            return Map.of("planId", planId.toString());
        }
        return Map.of("reviewId", reviewId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.COMMENT_MENTION;
    }
}
