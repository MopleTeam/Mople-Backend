package com.mople.dto.response.admin;

import com.mople.entity.meet.review.ReviewReport;

public record AdminReviewResponse(
        Long id,
        String reason,
        Long userId,
        Long reviewId
) {

    public AdminReviewResponse(ReviewReport reviewReport){
        this(
                reviewReport.getId(),
                reviewReport.getReason(),
                reviewReport.getReporterId(),
                reviewReport.getReviewId()
        );
    }
}
