package com.mople.dto.response.meet.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlanReviewDetailResponse(
        Long version,
        Long meetId,
        Long postId,
        Long reviewId,
        String meetName,
        String meetImage,
        Long creatorId,
        String reviewName,
        String address,
        String title,
        BigDecimal lat,
        BigDecimal lot,
        LocalDateTime reviewDateTime,
        int participantsCount,
        boolean register,
        List<ReviewImageResponse> images
) {
    public PlanReviewDetailResponse(
            PlanReview review,
            String meetName,
            String meetImage,
            int participantsCount,
            List<ReviewImage> images
    ) {
        this(
                review.getVersion(),
                review.getMeetId(),
                review.getPlanId(),
                review.getId(),
                meetName,
                meetImage,
                review.getCreatorId(),
                review.getName(),
                review.getAddress(),
                review.getTitle(),
                review.getLatitude(),
                review.getLongitude(),
                review.getPlanTime(),
                participantsCount,
                review.getUpload(),
                ReviewImageResponse.ofImageResponses(images)
        );
    }

    @Builder
    public record ReviewImageResponse(
            Long imageId,
            String reviewImg
    ) {
        public static List<ReviewImageResponse> ofImageResponses(List<ReviewImage> images) {
            return images.stream()
                    .map(
                    i -> ReviewImageResponse.builder()
                            .imageId(i.getId())
                            .reviewImg(i.getReviewImage())
                            .build()
                    ).toList();
        }
    }
}
