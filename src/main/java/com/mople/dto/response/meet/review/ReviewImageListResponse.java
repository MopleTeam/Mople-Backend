package com.mople.dto.response.meet.review;

import com.mople.entity.meet.review.ReviewImage;
import lombok.Builder;

import java.util.List;

@Builder
public record ReviewImageListResponse(
        Long imageId,
        String reviewImg
) {
    public static List<ReviewImageListResponse> ofReviewImageResponses(List<ReviewImage> reviewImages) {
        return reviewImages.stream().map(ReviewImageListResponse::ofReviewImageResponse).toList();
    }

    private static ReviewImageListResponse ofReviewImageResponse(ReviewImage reviewImage) {
        return ReviewImageListResponse.builder()
                .imageId(reviewImage.getId())
                .reviewImg(reviewImage.getReviewImage())
                .build();
    }
}
