package com.mople.meet.service;

import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.ReviewClientResponse;
import com.mople.dto.event.data.review.ReviewUpdateEventData;
import com.mople.dto.request.meet.review.ReviewImageDeleteRequest;
import com.mople.dto.request.meet.review.ReviewReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.review.PlanReviewDetailResponse;
import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.dto.response.meet.review.ReviewParticipantResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;
import com.mople.entity.meet.review.ReviewReport;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.image.service.ImageService;
import com.mople.meet.mapper.ReviewMapper;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.impl.review.ReviewRepositorySupport;
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
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int REVIEW_CURSOR_FIELD_COUNT = 1;

    private final PlanReviewRepository planReviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final ReviewRepositorySupport reviewRepositorySupport;

    private final ImageService imageService;
    private final ReviewMapper mapper;
    private final EntityReader reader;
    private final ApplicationEventPublisher publisher;

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewClientResponse> getAllMeetReviews(Long userId, Long meetId, CursorPageRequest request) {
        validateMember(userId, meetId);

        int size = request.getSafeSize();
        List<PlanReview> reviews = getReviews(meetId, request.cursor(), size);
        List<PlanReviewInfoResponse> reviewInfoResponses = reviews.stream()
                .map(PlanReviewInfoResponse::new)
                .toList();

        return buildReviewCursorPage(size, reviewInfoResponses);
    }

    private List<PlanReview> getReviews(Long meetId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return reviewRepositorySupport.findReviewFirstPage(meetId, size);
        }

        String[] decodeParts = CursorUtils.decode(encodedCursor, REVIEW_CURSOR_FIELD_COUNT);
        Long cursorId = Long.valueOf(decodeParts[0]);

        validateCursor(cursorId);

        return reviewRepositorySupport.findReviewNextPage(meetId, cursorId, size);
    }

    private void validateCursor(Long cursorId) {
        if (reviewRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    private CursorPageResponse<ReviewClientResponse> buildReviewCursorPage(int size, List<PlanReviewInfoResponse> reviewInfoResponses) {
        return buildCursorPage(
                reviewInfoResponses,
                size,
                c -> new String[]{
                        c.reviewId().toString()
                },
                ReviewClientResponse::ofInfos
        );
    }

    private void validateMember(Long userId, Long meetId) {
        User user = reader.findUser(userId);
        Meet meet = reader.findMeet(meetId);

        if (meet.matchMember(user.getId())) {
            throw new BadRequestException(NOT_MEMBER);
        }
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
