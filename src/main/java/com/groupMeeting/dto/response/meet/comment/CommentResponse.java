package com.groupMeeting.dto.response.meet.comment;

import com.groupMeeting.entity.meet.comment.CommentMention;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
        Long commentId,
        String content,
        Long postId,
        Long parentId,
        Integer replyCount,
        int likeCount,
        boolean likeByMe,
        User writer,
        List<CommentMention> mentions,
        LocalDateTime time
) {
    public CommentResponse(PlanComment comment) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getParentId(),
                comment.getReplyCount(),
                comment.getLikeCount(),
                comment.isLikedByMe(),
                comment.getWriter(),
                comment.getMentions(),
                comment.getWriteTime()
        );
    }
}
