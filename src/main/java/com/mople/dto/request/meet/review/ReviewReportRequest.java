package com.mople.dto.request.meet.review;

public record ReviewReportRequest(
        Long reviewId,
        String reason
) {
}
