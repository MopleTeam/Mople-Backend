package com.mople.dto.response.meet.comment;

import com.mople.entity.meet.comment.CommentStats;
import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentResponse(
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
        LocalDateTime time
) {
    public PostCommentResponse(MeetComment comment, CommentStats stats, User writer, List<User> mentions, boolean likedByMe) {
        this(
                comment.getId(),
                comment.getVersion(),
                comment.getContent(),
                comment.getTargetId(),
                comment.getParentId(),
                stats.getReplyCount(),
                stats.getLikeCount(),
                likedByMe,
                writer,
                mentions,
                comment.getWriteTime()
        );
    }
}
