package com.mople.dto.response.meet.comment;

import com.mople.entity.meet.comment.CommentStats;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record CommentUpdateResponse(
        Long commentId,
        Long version,
        String content,
        Long postId,
        Long parentId,
        Integer replyCount,
        Integer likeCount,
        boolean likedByMe,
        User writer,
        List<User> mentions,
        LocalDateTime time,
        boolean update
) {
    public CommentUpdateResponse(PlanComment comment, User writer, CommentStats stats, List<User> mentions, boolean likedByMe) {
        this(
                comment.getId(),
                comment.getVersion(),
                comment.getContent(),
                comment.getPostId(),
                comment.getParentId(),
                stats.getReplyCount(),
                stats.getLikeCount(),
                likedByMe,
                writer,
                mentions,
                comment.getWriteTime(),
                true
        );
    }
}
