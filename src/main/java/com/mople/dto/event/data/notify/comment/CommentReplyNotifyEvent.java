package com.mople.dto.event.data.notify.comment;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class CommentReplyNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long postId;
    private final Long planId;
    private final Long reviewId;
    private final String senderNickname;
    private final List<Long> targetIds;

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
    public List<Long> targetIds() {
        return this.targetIds;
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.COMMENT_REPLY;
    }
}
