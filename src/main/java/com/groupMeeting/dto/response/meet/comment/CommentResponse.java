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
        boolean likedByMe,
        User writer,
        List<User> mentions,
        LocalDateTime time
) {
    public CommentResponse(PlanComment comment, List<User> mentions, boolean likedByMe) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getPostId(),
                comment.getParentId(),
                comment.getReplyCount(),
                comment.getLikeCount(),
                likedByMe,
                comment.getWriter(),
                mentions,
                comment.getWriteTime()
        );
    }
}
