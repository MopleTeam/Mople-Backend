package com.mople.meet.service;

import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.ReviewClientResponse;
import com.mople.dto.event.data.review.ReviewUpdateEventData;
import com.mople.dto.request.meet.review.ReviewImageDeleteRequest;
import com.mople.dto.request.meet.review.ReviewReportRequest;
import com.mople.dto.response.meet.review.PlanReviewDetailResponse;
import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.dto.response.meet.review.ReviewParticipantResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;
import com.mople.entity.meet.review.ReviewReport;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.image.service.ImageService;
import com.mople.meet.mapper.ReviewMapper;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.meet.repository.review.ReviewImageRepository;
import com.mople.dto.response.meet.review.PlanReviewInfoResponse;
import com.mople.meet.repository.review.ReviewReportRepository;

import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.dto.client.ReviewClientResponse.*;
import static com.mople.global.enums.ExceptionReturnCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final PlanReviewRepository planReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final ImageService imageService;
    private final ReviewMapper mapper;
    private final EntityReader reader;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public List<ReviewClientResponse> getAllMeetReviews(Long meetId) {
        Meet meet = reader.findMeetAndReview(meetId);

        return ofInfos(
                meet.getReviews()
                        .stream()
                        .map(PlanReviewInfoResponse::new)
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public ReviewClientResponse getReviewDetail(Long reviewId) {
        PlanReview review = planReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        return ofDetail(
                new PlanReviewDetailResponse(review),
                commentRepositorySupport.countComment(review.getPlanId())
        );
    }

    @Transactional(readOnly = true)
    public ReviewClientResponse getReviewDetailByPost(Long postId) {
        PlanReview review = planReviewRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        return ofDetail(
                new PlanReviewDetailResponse(review),
                commentRepositorySupport.countComment(review.getPlanId())
        );
    }

    @Transactional
    public void removeReview(Long reviewId) {
        planReviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public ReviewParticipantResponse getReviewParticipants(Long reviewId) {
        return new ReviewParticipantResponse(reader.findReview(reviewId));
    }

    @Transactional(readOnly = true)
    public List<ReviewImageListResponse> getReviewImages(Long reviewId) {
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewId(reviewId);

        if (reviewImages.isEmpty()) {
            return List.of();
        }

        return mapper.getReviewImages(reviewImages);
    }

    @Transactional
    public List<ReviewImageListResponse> removeReviewImages(Long reviewId, ReviewImageDeleteRequest request) {
        List<ReviewImage> reviewImages = reviewImageRepository.getReviewImages(request.reviewImages(), reviewId);

        if (request.reviewImages().isEmpty()) {
            return List.of();
        }

        PlanReview review = reviewImages.get(0).getReview();

        reviewImages.forEach(i -> {
            imageService.deleteImage(i.getReviewImage());
            i.getReview().removeImage(i);
        });

        reviewImageRepository.deleteAll(reviewImages);

        return mapper.getReviewImages(review.getImages());
    }

    @Transactional
    public List<String> storeReviewImages(List<String> images, Long reviewId) {
        PlanReview review =
                planReviewRepository.findReview(reviewId)
                        .orElseThrow(() -> new BadRequestException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        if (!review.getUpload()) {
            review.updateUpload();

            publisher.publishEvent(
                    NotifyEventPublisher.reviewUpdate(
                            ReviewUpdateEventData.builder()
                                    .meetId(review.getMeet().getId())
                                    .meetName(review.getMeet().getName())
                                    .reviewId(review.getId())
                                    .reviewName(review.getName())
                                    .creatorId(review.getCreatorId())
                                    .build()
                    )
            );
        }

        reviewImageRepository.saveAll(
                images.stream().map(
                        imageName -> {
                            ReviewImage image = ReviewImage
                                    .builder()
                                    .reviewImage(imageName)
                                    .build();
                            review.updateImage(image);

                            return image;
                        }
                ).toList()
        );

        return images;
    }

    public void reviewReport(@NotNull Long userId, ReviewReportRequest request) {
        reviewReportRepository.save(
                ReviewReport.builder()
                        .reason(request.reason())
                        .reviewId(request.reviewId())
                        .reporterId(userId)
                        .build()
        );
    }
}
