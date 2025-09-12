package com.mople.meet.service;

import com.mople.core.exception.custom.*;
import com.mople.dto.client.ReviewClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.event.data.domain.review.ReviewImageRemoveEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewUploadEvent;
import com.mople.dto.request.meet.review.ReviewImageDeleteRequest;
import com.mople.dto.request.meet.review.ReviewReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.review.PlanReviewDetailResponse;
import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.dto.response.user.UserInfo;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.ReviewImage;
import com.mople.entity.meet.review.ReviewReport;
import com.mople.entity.user.User;
import com.mople.global.utils.cursor.CursorUtils;
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

import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.mople.dto.client.ReviewClientResponse.*;
import static com.mople.dto.client.UserRoleClientResponse.ofParticipants;
import static com.mople.dto.response.meet.review.ReviewImageListResponse.ofReviewImageResponses;
import static com.mople.dto.response.user.UserInfo.ofMap;
import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.*;
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
    private final UserRepository userRepository;

    private final EntityReader reader;
    private final OutboxService outboxService;

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<ReviewClientResponse> getReviewList(Long userId, Long meetId, CursorPageRequest request) {
        reader.findUser(userId);
        reader.findMeet(meetId);

        if (!memberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new AuthException(NOT_MEMBER);
        }

        int size = request.getSafeSize();
        List<PlanReview> reviews = getReviews(meetId, request.cursor(), size);
        List<PlanReviewInfoResponse> reviewInfoResponses = reviews.stream()
                .map((r) -> {
                    Integer participantCount = participantRepository.countByReviewId(r.getId());
                    List<ReviewImage> images = reviewImageRepository.findByReviewId(r.getId());

                    return new PlanReviewInfoResponse(r, participantCount, images);
                })
                .toList();

        return FlatCursorPageResponse.of(
                planReviewRepository.countByMeetIdAndStatus(meetId, Status.ACTIVE),
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
        PlanReview review = reader.findReview(reviewId);
        Meet meet = reader.findMeet(review.getMeetId());

        Integer participantCount = participantRepository.countByReviewId(reviewId);
        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);

        Integer commentCount = commentRepositorySupport.countComment(review.getPlanId());

        return ofDetail(
                new PlanReviewDetailResponse(
                        review,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount,
                        images
                ),
                commentCount
        );
    }

    @Transactional(readOnly = true)
    public ReviewClientResponse getReviewDetailByPost(Long postId) {
        PlanReview review = reader.findReviewByPostId(postId);
        Meet meet = reader.findMeet(review.getMeetId());

        Integer participantCount = participantRepository.countByReviewId(review.getId());
        List<ReviewImage> images = reviewImageRepository.findByReviewId(review.getId());

        Integer commentCount = commentRepositorySupport.countComment(review.getPlanId());

        return ofDetail(
                new PlanReviewDetailResponse(
                        review,
                        meet.getName(),
                        meet.getMeetImage(),
                        participantCount,
                        images
                ),
                commentCount
        );
    }

    @Transactional
    public void removeReview(Long userId, Long reviewId, long baseVersion) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (review.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        if (!Objects.equals(baseVersion, review.getVersion())) {
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, getVersion(review.getId()));
        }

        int updated = planReviewRepository.softDelete(Status.DELETED, reviewId, userId, baseVersion, LocalDateTime.now());
        if (updated == 0) {
            throw new ConcurrencyConflictException(REQUEST_CONFLICT, getVersion(review.getId()));
        }

        ReviewSoftDeletedEvent deletedEvent = ReviewSoftDeletedEvent.builder()
                .planId(review.getPlanId())
                .reviewId(reviewId)
                .reviewDeletedBy(userId)
                .build();

        outboxService.save(REVIEW_SOFT_DELETED, REVIEW, reviewId, deletedEvent);
    }

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<UserRoleClientResponse> getParticipantList(Long userId, Long reviewId, CursorPageRequest request) {
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
                participantRepository.countByReviewId(reviewId),
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
        List<Long> userIds = participants.stream()
                .map(PlanParticipant::getUserId)
                .toList();

        Map<Long, UserInfo> userInfoById = ofMap(userRepository.findByIdInAndStatus(userIds, Status.ACTIVE));

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
                list -> ofParticipants(list, userInfoById, hostId, creatorId)
        );
    }

    private void validateParticipantCursor(String cursorNickname, Long cursorId) {
        if (participantRepositorySupport.isCursorInvalid(cursorNickname, cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewImageListResponse> getReviewImages(Long reviewId) {
        reader.findReview(reviewId);
        List<ReviewImage> reviewImages = reviewImageRepository.findByReviewId(reviewId);

        if (reviewImages.isEmpty()) {
            return List.of();
        }

        return ofReviewImageResponses(reviewImages);
    }

    @Transactional
    public List<ReviewImageListResponse> removeReviewImages(
            Long userId,
            Long reviewId,
            ReviewImageDeleteRequest request
    ) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (review.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        if (request.reviewImages() == null || request.reviewImages().isEmpty()) {
            return List.of();
        }

        List<ReviewImage> reviewImages = reviewImageRepository.getReviewImages(request.reviewImages(), reviewId);

        if (reviewImages.isEmpty()) {
            List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewImages.get(0).getReviewId());
            return ofReviewImageResponses(images);
        }

        reviewImageRepository.deleteAll(reviewImages);

        reviewImages.forEach(i -> {
            ReviewImageRemoveEvent removeEvent = ReviewImageRemoveEvent.builder()
                    .reviewId(reviewId)
                    .imageUrl(i.getReviewImage())
                    .imageDeletedBy(userId)
                    .build();

            outboxService.save(REVIEW_IMAGE_REMOVE, REVIEW, reviewId, removeEvent);
        });

        planReviewRepository.upVersion(review.getId());

        List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewImages.get(0).getReviewId());
        return ofReviewImageResponses(images);
    }

    @Transactional
    public List<String> storeReviewImages(
            Long userId,
            List<String> images,
            Long reviewId
    ) {
        reader.findUser(userId);
        PlanReview review = reader.findReview(reviewId);

        if (review.isCreator(userId)) {
            throw new AuthException(NOT_CREATOR);
        }

        if (images == null || images.isEmpty()) {
            return images;
        }

        reviewImageRepository.saveAll(
                images.stream()
                        .map(imageName ->
                                ReviewImage.builder()
                                        .reviewId(reviewId)
                                        .reviewImage(imageName)
                                        .build()
                        )
                        .toList()
        );

        if (!review.getUpload()) {
            ReviewUploadEvent uploadEvent = ReviewUploadEvent.builder()
                    .reviewId(review.getId())
                    .reviewUpdatedBy(userId)
                    .build();

            outboxService.save(REVIEW_UPLOAD, REVIEW, review.getId(), uploadEvent);
        }

        planReviewRepository.markUploaded(review.getId());
        planReviewRepository.upVersion(review.getId());

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

    public long getVersion(Long reviewId) {
        return planReviewRepository.findVersionById(reviewId);
    }
}
