package com.mople.dto.response.meet.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

public record PlanReviewInfoResponse(
        Long reviewId,
        String reviewName,
        LocalDateTime reviewDateTime,
        Long creatorId,
        int participantsCount,
        List<PlanReviewDetailResponse.ReviewImageResponse> images
) {
    public PlanReviewInfoResponse(PlanReview review) {
        this(
                review.getId(),
                review.getName(),
                review.getPlanTime(),
                review.getCreatorId(),
                review.getParticipants().size(),
                review.getImages().stream().map(PlanReviewDetailResponse.ReviewImageResponse::new).toList()
        );
    }
}
