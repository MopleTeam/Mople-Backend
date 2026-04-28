package com.mople.dto.response.meet.comment;

import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.user.User;

import java.time.LocalDateTime;

public record NoticeCommentResponse(
        Long commentId,
        Long version,
        String content,
        Long noticeId,
        Long parentId,
        User writer,
        LocalDateTime time
) {
    public NoticeCommentResponse(MeetComment comment, User writer) {
        this(
                comment.getId(),
                comment.getVersion(),
                comment.getContent(),
                comment.getTargetId(),
                comment.getParentId(),
                writer,
                comment.getWriteTime()
        );
    }
}
