package com.groupMeeting.dto.response.admin;

import com.groupMeeting.entity.meet.comment.CommentReport;

public record AdminCommentResponse(
        Long id,
        String reason,
        Long userId,
        Long commentId
) {

    public AdminCommentResponse(CommentReport commentReport) {
        this(
                commentReport.getId(),
                commentReport.getReason(),
                commentReport.getReporterId(),
                commentReport.getCommentId()
        );
    }
}
