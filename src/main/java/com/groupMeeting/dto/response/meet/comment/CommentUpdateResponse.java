package com.groupMeeting.dto.response.meet.comment;

import com.groupMeeting.entity.meet.comment.PlanComment;

import java.time.LocalDateTime;

public record CommentUpdateResponse(
        Long commentId,
        Long postId,
        Long writerId,
        String writerName,
        String writerImage,
        String content,
        LocalDateTime time,
        boolean update
) {
    public CommentUpdateResponse(PlanComment comment) {
        this(
                comment.getId(),
                comment.getPostId(),
                comment.getWriterId(),
                comment.getWriterNickname(),
                comment.getWriterImg(),
                comment.getContent(),
                comment.getWriteTime(),
                true
        );
    }
}
