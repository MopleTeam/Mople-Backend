package com.groupMeeting.dto.response.meet.comment;

import com.groupMeeting.entity.meet.comment.PlanComment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long writerId,
        String writerName,
        String writerImage,
        String content,
        LocalDateTime time
) {
    public CommentResponse(PlanComment comment, String userName, String userImage) {
        this(
                comment.getId(),
                comment.getPostId(),
                comment.getWriterId(),
                userName,
                userImage,
                comment.getContent(),
                comment.getWriteTime()
        );
    }
}
