package com.mople.dto.request.meet.comment;

public record CommentReportRequest(
    Long commentId,
    String reason
) {
}
