package com.mople.dto.event.data.notify.comment;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.Map;

@Builder
public record CommentReplyNotifyEvent(
        String meetName,
        Long postId,
        String senderNickname
) implements NotifyEvent {

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 새로운 대댓글 👀",
                meetName + "에서 " + senderNickname + "님이 답글을 남겼어요!"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("postId", postId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.COMMENT_REPLY;
    }
}
