package com.mople.dto.event.data.comment;

import com.mople.dto.event.data.EventData;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class CommentMentionEventData implements EventData {

    private final Long postId;
    private final String postName;
    private final Long commentId;
    private final String commentContent;
    private final Long senderId;
    private final String senderNickname;
    private final List<Long> originMentions;

    @Override
    public String getTitle() {
        return postName + "에서 " + senderNickname + "님이 회원님을 멘션했어요!";
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
