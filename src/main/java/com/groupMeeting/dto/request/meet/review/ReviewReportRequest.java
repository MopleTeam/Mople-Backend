package com.groupMeeting.dto.request.meet.review;

public record ReviewReportRequest(
        Long reviewId,
        String reason
) {
}
