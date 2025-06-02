package com.groupMeeting.dto.request.meet.comment;

public record CommentReportRequest(
    Long commentId,
    String reason
) {
}
