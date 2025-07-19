package com.mople.dto.event.data.comment;

import com.mople.dto.event.data.EventData;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class CommentReplyEventData implements EventData {

    private final Long postId;
    private final String postName;
    private final Long commentId;
    private final String commentContent;
    private final Long senderId;
    private final String senderNickname;
    private final Long parentCommentId;

    @Override
    public String getTitle() {
        return postName + "에서 " + senderNickname + "님이 답글을 남겼어요!";
    }

    @Override
    public String getBody() {
        return commentContent;
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("commentId", commentId.toString());
    }
}
