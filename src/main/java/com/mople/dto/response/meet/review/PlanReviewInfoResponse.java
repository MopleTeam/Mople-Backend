package com.mople.dto.response.meet.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;

import java.time.LocalDateTime;
import java.util.List;

import static com.mople.dto.response.meet.review.PlanReviewDetailResponse.ReviewImageResponse.ofImageResponses;

public record PlanReviewInfoResponse(
        Long reviewId,
        Long version,
        String reviewName,
        LocalDateTime reviewDateTime,
        Long creatorId,
        int participantsCount,
        List<PlanReviewDetailResponse.ReviewImageResponse> images
) {
    public PlanReviewInfoResponse(PlanReview review, int participantsCount, List<ReviewImage> images) {
        this(
                review.getId(),
                review.getVersion(),
                review.getName(),
                review.getPlanTime(),
                review.getCreatorId(),
                participantsCount,
                ofImageResponses(images)
        );
    }
}
