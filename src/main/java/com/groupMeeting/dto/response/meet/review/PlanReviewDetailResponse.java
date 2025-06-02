package com.groupMeeting.dto.response.meet.review;

import com.groupMeeting.entity.meet.review.PlanReview;
import com.groupMeeting.entity.meet.review.ReviewImage;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlanReviewDetailResponse(
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
    public PlanReviewDetailResponse(PlanReview review) {
        this(
                review.getMeet().getId(),
                review.getPlanId(),
                review.getId(),
                review.getMeet().getName(),
                review.getMeet().getMeetImage(),
                review.getCreatorId(),
                review.getName(),
                review.getAddress(),
                review.getTitle(),
                review.getLatitude(),
                review.getLongitude(),
                review.getPlanTime(),
                review.getParticipants().size(),
                review.getUpload(),
                review.getImages().stream().map(ReviewImageResponse::new).toList()
        );
    }

    public record ReviewImageResponse(
            Long imageId,
            String reviewImg
    ) {
        public ReviewImageResponse(ReviewImage reviewImage) {
            this(reviewImage.getId(), reviewImage.getReviewImage());
        }
    }
}
