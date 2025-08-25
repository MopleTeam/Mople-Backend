package com.mople.dto.event.data.notify.comment;

import com.mople.dto.event.data.notify.NotifyEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class CommentMentionNotifyEvent implements NotifyEvent {

    private final String meetName;
    private final Long postId;
    private final Long commentId;
    private final String commentContent;
    private final Long senderId;
    private final String senderNickname;
    private final List<Long> originMentions;

    @Override
    public String getTitle() {
        return meetName + "의 새로운 멘션 👀";
    }

    @Override
    public String getBody() {
        return meetName + "에서 " + senderNickname + "님이 회원님을 멘션했어요!";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("commentId", commentId.toString());
    }
}
