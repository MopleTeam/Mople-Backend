package com.mople.meet.service;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.ReviewClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.domain.review.ReviewUpdateEvent;
import com.mople.dto.request.meet.review.ReviewImageDeleteRequest;
import com.mople.dto.request.meet.review.ReviewReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.review.PlanReviewDetailResponse;
import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;
import com.mople.entity.meet.review.ReviewReport;
import com.mople.entity.user.User;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.image.service.ImageService;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.impl.plan.ParticipantRepositorySupport;
import com.mople.meet.repository.impl.review.ReviewRepositorySupport;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.meet.repository.review.ReviewImageRepository;
import com.mople.dto.response.meet.review.PlanReviewInfoResponse;
import com.mople.meet.repository.review.ReviewReportRepository;

import com.mople.outbox.repository.OutboxEventRepository;
import com.mople.outbox.service.OutboxService;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.mople.dto.client.ReviewClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofParticipants;
import static com.mople.dto.response.meet.review.ReviewImageListResponse.ofReviewImageResponses;
import static com.mople.global.enums.AggregateType.REVIEW;
import static com.mople.global.enums.EventTypeNames.*;
import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int REVIEW_CURSOR_FIELD_COUNT = 1;
    private static final int REVIEW_PARTICIPANT_CURSOR_FIELD_COUNT = 2;

    private final PlanReviewRepository planReviewRepository;
    private final PlanParticipantRepository participantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewReportRepository reviewReportRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final ReviewRepositorySupport reviewRepositorySupport;
    private final MeetMemberRepository memberRepository;
    private final ParticipantRepositorySupport participantRepositorySupport;

    private final ImageService imageService;
    private final EntityReader reader;
    private final OutboxService outboxService;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<ReviewClientResponse> getAllMeetReviews(Long userId, Long meetId, CursorPageRequest request) {
        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<PlanReview> reviews = getReviews(meetId, request.cursor(), size);
        List<PlanReviewInfoResponse> reviewInfoResponses = reviews.stream()
                .map((r) -> {
                    List<PlanParticipant> participants = participantRepository.findParticipantsByReviewId(r.getId());
                    List<ReviewImage> images = reviewImageRepository.findByReviewId(r.getId());

                    return new PlanReviewInfoResponse(r, participants.size(), images);
                })
                .toList();

        return FlatCursorPageResponse.of(
                reviewRepositorySupport.countReviews(meetId),
                buildReviewCursorPage(size, reviewInfoResponses)
        );
    }

    private List<PlanReview> getReviews(Long meetId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, REVIEW_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            validateReviewCursor(cursorId);
        }

        return reviewRepositorySupport.findReviewPage(meetId, cursorId, size);
    }

    private void validateReviewCursor(Long cursorId) {
        if (reviewRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    private CursorPageResponse<ReviewClientResponse> buildReviewCursorPage(int size, List<PlanReviewInfoResponse> reviewInfoResponses) {
        return buildCursorPage(
                reviewInfoResponses,
                size,
                r -> new String[]{
                        r.reviewId().toString()
                },
                ReviewClientResponse::ofInfos
        );
    }

    @Transactional(readOnly = true)
    public ReviewClientResponse getReviewDetail(Long reviewId) {
        PlanReview review = planReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        Meet meet = reader.findMeet(review.getMeetId());

        List<PlanParticipant> participants = participantRepository.findParticipantsByReviewId(reviewId);

        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);

        return ofDetail(
                new PlanReviewDetailResponse(
                        review,
                        meet.getName(),
                        meet.getMeetImage(),
                        participants.size(),
                        images
                ),
                commentRepositorySupport.countComment(review.getPlanId())
        );
    }

    @Transactional(readOnly = true)
    public ReviewClientResponse getReviewDetailByPost(Long postId) {
        PlanReview review = planReviewRepository.findReviewByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        Meet meet = reader.findMeet(review.getMeetId());

        List<PlanParticipant> participants = participantRepository.findParticipantsByReviewId(review.getId());

        List<ReviewImage> images = reviewImageRepository.findByReviewId(review.getId());

        return ofDetail(
                new PlanReviewDetailResponse(
                        review,
                        meet.getName(),
                        meet.getMeetImage(),
                        participants.size(),
                        images
                ),
                commentRepositorySupport.countComment(review.getPlanId())
        );
    }

    @Transactional
    public void removeReview(Long userId, Long reviewId, Long version) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (review.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        if (!Objects.equals(version, review.getVersion())) {
            throw new AsyncException(REQUEST_CONFLICT);
        }

        outboxEventRepository.deleteEventByAggregateType(REVIEW.name(), review.getId());
        participantRepository.deleteByReviewId(review.getId());

        planReviewRepository.deleteById(reviewId);
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<UserRoleClientResponse> getReviewParticipants(Long userId, Long reviewId, CursorPageRequest request) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (!memberRepository.existsByMeetIdAndUserId(review.getMeetId(), userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        Meet meet = reader.findMeet(review.getMeetId());

        int size = request.getSafeSize();
        Long hostId = meet.getCreatorId();
        Long creatorId = review.getCreatorId();
        List<PlanParticipant> participants = getReviewParticipants(reviewId, hostId, creatorId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                participantRepositorySupport.countReviewParticipants(reviewId),
                buildParticipantCursorPage(size, participants, hostId, creatorId)
        );
    }

    private List<PlanParticipant> getReviewParticipants(Long reviewId, Long hostId, Long creatorId, String encodedCursor, int size) {

        MemberCursor cursor = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, REVIEW_PARTICIPANT_CURSOR_FIELD_COUNT);

            String cursorNickname = decodeParts[0];
            Long cursorId = Long.valueOf(decodeParts[1]);
            validateParticipantCursor(cursorNickname, cursorId);

            cursor = new MemberCursor(cursorNickname, cursorId, hostId, creatorId);
        }

        return participantRepositorySupport.findReviewParticipantPage(reviewId, hostId, creatorId, cursor, size);
    }

    private CursorPageResponse<UserRoleClientResponse> buildParticipantCursorPage(int size, List<PlanParticipant> participants, Long hostId, Long creatorId) {
        return buildCursorPage(
                participants,
                size,
                p -> {
                    User user = reader.findUser(p.getUserId());
                    return new String[]{
                            user.getNickname(),
                            p.getId().toString()
                    };
                },
                list -> ofParticipants(list, hostId, creatorId)
        );
    }

    private void validateParticipantCursor(String cursorNickname, Long cursorId) {
        if (participantRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewImageListResponse> getReviewImages(Long reviewId) {
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewId(reviewId);

        if (reviewImages.isEmpty()) {
            return List.of();
        }

        return ofReviewImageResponses(reviewImages);
    }

    @Transactional
    public List<ReviewImageListResponse> removeReviewImages(Long reviewId, ReviewImageDeleteRequest request) {
        reader.findReview(reviewId);

        List<ReviewImage> reviewImages = reviewImageRepository.getReviewImages(request.reviewImages(), reviewId);

        if (request.reviewImages().isEmpty()) {
            return List.of();
        }

        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewImages.get(0).getReviewId());

        reviewImages.forEach(i -> imageService.deleteImage(i.getReviewImage()));
        reviewImageRepository.deleteAll(reviewImages);

        return ofReviewImageResponses(images);
    }

    @Transactional
    public List<String> storeReviewImages(Long userId, List<String> images, Long reviewId) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (review.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        reviewImageRepository.saveAll(
                images.stream()
                        .map(imageName -> ReviewImage.builder()
                                .reviewId(reviewId)
                                .reviewImage(imageName)
                                .build()
                        )
                        .toList()
        );

        ReviewUpdateEvent updateEvent = ReviewUpdateEvent.builder()
                .reviewId(review.getId())
                .isFirstUpload(review.getUpload())
                .reviewUpdatedBy(userId)
                .build();

        outboxService.save(REVIEW_UPDATE, REVIEW, review.getId(), updateEvent);

        planReviewRepository.uploadedAtFirst(review.getId());

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
