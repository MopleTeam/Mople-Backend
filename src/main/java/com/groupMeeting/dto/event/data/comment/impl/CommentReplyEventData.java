package com.groupMeeting.dto.event.data.comment.impl;

import com.groupMeeting.dto.event.data.comment.CommentEventData;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class CommentReplyEventData implements CommentEventData {

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
