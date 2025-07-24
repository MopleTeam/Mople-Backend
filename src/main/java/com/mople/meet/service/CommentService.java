package com.mople.meet.service;

import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.CommentClientResponse;
import com.mople.dto.event.data.comment.CommentMentionEventData;
import com.mople.dto.event.data.comment.CommentReplyEventData;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.response.meet.comment.CommentResponse;
import com.mople.dto.response.meet.comment.CommentUpdateResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.CursorPage;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.comment.CommentLike;
import com.mople.entity.meet.comment.CommentMention;
import com.mople.entity.meet.comment.CommentReport;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentLikeRepository;
import com.mople.meet.repository.comment.CommentMentionRepository;
import com.mople.meet.repository.comment.CommentReportRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.dto.request.meet.comment.CommentReportRequest;

import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.mople.dto.client.CommentClientResponse.*;
import static com.mople.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PlanCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentReportRepository commentReportRepository;
    private final CommentMentionRepository mentionRepository;
    private final CommentLikeRepository likeRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    private final ApplicationEventPublisher publisher;
    private final NotificationUserReader userReader;
    private final EntityReader reader;

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentList(Long userId, Long postId, String cursor, int size) {
        validatePostIdExists(postId);
        validateMember(userId, postId);

        List<CommentResponse> commentResponses = getComments(userId, postId, cursor, size);
        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getComments(Long userId, Long postId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            List<PlanComment> commentFirstPage = commentRepositorySupport.findCommentFirstPage(postId, size);
            return getResponseAddedLikedByMe(userId, commentFirstPage);
        }

        Long cursor = CursorUtils.decode(encodedCursor);
        if (commentRepositorySupport.validCursor(cursor)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }

        List<PlanComment> commentNextPage = commentRepositorySupport.findCommentNextPage(postId, cursor, size);
        return getResponseAddedLikedByMe(userId, commentNextPage);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentReplyList(Long userId, Long postId, Long commentId, String cursor, int size) {
        validatePostIdExists(postId);
        validateMember(userId, postId);

        List<CommentResponse> commentResponses = getCommentReplies(userId, postId, commentId, cursor, size);
        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getCommentReplies(Long userId, Long postId, Long commentId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            List<PlanComment> commentReplyFirstPage = commentRepositorySupport.findCommentReplyFirstPage(postId, commentId, size);
            return getResponseAddedLikedByMe(userId, commentReplyFirstPage);
        }

        Long cursor = CursorUtils.decode(encodedCursor);
        if (commentRepositorySupport.validCursor(cursor)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }

        List<PlanComment> commentReplyNextPage = commentRepositorySupport.findCommentReplyNextPage(postId, commentId, cursor, size);
        return getResponseAddedLikedByMe(userId, commentReplyNextPage);
    }

    private List<CommentResponse> getResponseAddedLikedByMe(Long userId, List<PlanComment> comments) {
        List<Long> commentIds = comments.stream()
                .map(PlanComment::getId)
                .toList();

        List<Long> likedCommentIds = likeRepository.findLikedCommentIds(userId, commentIds);

        return comments.stream()
                .map(comment -> {
                    boolean likedByMe = likedCommentIds.contains(comment.getId());
                    List<User> mentionedUsers = findMentionedUsers(comment.getId());

                    return new CommentResponse(comment, mentionedUsers, likedByMe);
                })
                .toList();
    }

    private CursorPageResponse<CommentClientResponse> buildCursorPage(int size, List<CommentResponse> commentResponses) {
        boolean hasNext = commentResponses.size() > size;
        commentResponses = hasNext ? commentResponses.subList(0, size) : commentResponses;

        String nextCursor = hasNext && !commentResponses.isEmpty()
                ? CursorUtils.encode(commentResponses.get(commentResponses.size() - 1).commentId())
                : null;

        CursorPage page = CursorPage.builder()
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(commentResponses.size())
                .build();

        return CursorPageResponse.of(ofComments(commentResponses), page);
    }

    @Transactional
    public CommentClientResponse createComment(Long userId, Long postId, CommentCreateRequest request) {
        User writer = reader.findUser(userId);

        validatePostIdExists(postId);
        validateMember(userId, postId);

        PlanComment comment = PlanComment.ofParent(
                request.contents(),
                postId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        commentRepository.save(comment);
        createMentions(request.mentions(), comment.getId());

        publishMentionEvent(null, request.mentions(), comment);

        boolean likedByMe = likeRepository.existsByUserIdAndCommentId(userId, comment.getId());
        List<User> mentionedUsers = findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    @Transactional
    public CommentClientResponse createCommentReply(Long userId, Long postId, Long parentCommentId, CommentCreateRequest request) {
        User writer = reader.findUser(userId);

        validatePostIdExists(postId);
        validateMember(userId, postId);

        PlanComment parentComment = validateParentComment(parentCommentId);

        PlanComment comment = PlanComment.ofChild(
                request.contents(),
                postId,
                parentCommentId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        commentRepository.save(comment);
        createMentions(request.mentions(), comment.getId());

        parentComment.increaseReplyCount();

        publishMentionEvent(null, request.mentions(), comment);
        publishReplyEvent(request.mentions(), comment, parentComment);

        boolean likedByMe = likeRepository.existsByUserIdAndCommentId(userId, comment.getId());
        List<User> mentionedUsers = findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    private void validatePostIdExists(Long postId) {
        boolean existsInPlan = planRepository.existsById(postId);
        boolean existsInReview = reviewRepository.findReviewByPostId(postId).isPresent();

        if (!existsInPlan && !existsInReview) {
            throw new ResourceNotFoundException(NOT_FOUND_POST);
        }
    }

    private void validateMember(Long userId, Long postId) {
        User user = reader.findUser(userId);

        boolean isMember = false;

        if (planRepository.existsById(postId)) {
            MeetPlan plan = reader.findPlan(postId);
            isMember = plan.getMeet().getMembers()
                    .stream()
                    .map(MeetMember::getUser)
                    .anyMatch(member -> Objects.equals(member.getId(), user.getId()));
        }

        if (!planRepository.existsById(postId)) {
            PlanReview review = reviewRepository.findReviewByPostId(postId)
                    .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

            isMember = review.getMeet().getMembers()
                    .stream()
                    .map(MeetMember::getUser)
                    .anyMatch(member -> Objects.equals(member.getId(), user.getId()));
        }

        if (!isMember) {
            throw new ResourceNotFoundException(NOT_MEMBER);
        }
    }

    private PlanComment validateParentComment(Long commentId) {
        PlanComment parentComment = reader.findComment(commentId);

        if (parentComment.getParentId() != null) {
            throw new ResourceNotFoundException(NOT_PARENT_COMMENT);
        }

        return parentComment;
    }

    @Transactional
    public CommentClientResponse updateComment(Long userId, Long commentId, CommentCreateRequest request) {
        PlanComment comment = reader.findComment(commentId);
        User user = reader.findUser(userId);

        validateWriter(comment, user);

        List<Long> originMentions = mentionRepository.findUserIdByCommentId(comment.getId());

        comment.updateContent(request.contents());
        updateMentions(request.mentions(), comment.getId());

        publishMentionEvent(originMentions, request.mentions(), comment);

        boolean likedByMe = likeRepository.existsByUserIdAndCommentId(userId, comment.getId());
        List<User> mentionedUsers = findMentionedUsers(comment.getId());

        return ofUpdate(new CommentUpdateResponse(comment, mentionedUsers, likedByMe));
    }

    private static void validateWriter(PlanComment comment, User user) {
        if (comment.matchWriter(user.getId())) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }
    }

    private void createMentions(List<Long> mentions, Long commentId) {
        if (mentions == null || mentions.isEmpty()) return;

        for (Long userId : mentions) {
            User mentionedUser = reader.findUser(userId);

            CommentMention mention = CommentMention.builder()
                    .userId(mentionedUser.getId())
                    .commentId(commentId)
                    .build();
            mentionRepository.save(mention);
        }
    }

    private void updateMentions(List<Long> mentions, Long commentId) {
        mentionRepository.deleteByCommentId(commentId);
        createMentions(mentions, commentId);
    }

    private void publishMentionEvent(List<Long> originMentions, List<Long> newMentions, PlanComment comment) {
        if (newMentions == null || newMentions.isEmpty()) return;

        publisher.publishEvent(
                NotifyEventPublisher.commentMention(
                        CommentMentionEventData.builder()
                                .postId(comment.getPostId())
                                .postName(getPostName(comment.getPostId()))
                                .commentId(comment.getId())
                                .commentContent(comment.getContent())
                                .senderId(comment.getWriter().getId())
                                .senderNickname(comment.getWriter().getNickname())
                                .originMentions(originMentions)
                                .build()
                )
        );
    }

    private void publishReplyEvent(List<Long> mentions, PlanComment comment, PlanComment parentComment) {
        boolean parentIsMentioned = false;

        if (mentions != null && !mentions.isEmpty()) {
            List<User> mentionedUsers = userReader.findMentionedUsers(comment.getWriter().getId(), comment.getId());
            User parentCommentWriter = parentComment.getWriter();

            parentIsMentioned = mentionedUsers.stream().anyMatch(user -> user.getId().equals(parentCommentWriter.getId()));
        }

        if (!parentIsMentioned) {
            publisher.publishEvent(
                    NotifyEventPublisher.commentReply(
                            CommentReplyEventData.builder()
                                    .postId(comment.getPostId())
                                    .postName(getPostName(comment.getPostId()))
                                    .commentId(comment.getId())
                                    .commentContent(comment.getContent())
                                    .senderId(comment.getWriter().getId())
                                    .senderNickname(comment.getWriter().getNickname())
                                    .parentCommentId(comment.getParentId())
                                    .build()
                    )
            );
        }
    }

    private String getPostName(Long postId) {
        return planRepository.findPlanAndMeet(postId)
                .map(MeetPlan::getName)
                .orElseGet(() ->
                        reviewRepository.findReviewByPostId(postId)
                                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW))
                                .getName()
                );
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = reader.findUser(userId);
        PlanComment comment = reader.findComment(commentId);

        validateWriter(comment, user);

        if (comment.isChildComment()) {
            PlanComment parentComment = reader.findComment(comment.getParentId());

            deleteSingleComment(comment);
            parentComment.decreaseReplyCount();
            return;
        }

        List<Long> replyIds = commentRepository
                .findAllByParentId(comment.getId())
                .stream()
                .map(PlanComment::getId)
                .toList();

        if (!replyIds.isEmpty()) {
            deleteCommentsByIds(replyIds);
        }

        deleteSingleComment(comment);
    }

    private void deleteCommentsByIds(List<Long> replyIds) {
        likeRepository.deleteByCommentIdIn(replyIds);
        mentionRepository.deleteByCommentIdIn(replyIds);
        commentRepository.deleteByIdIn(replyIds);
    }

    private void deleteSingleComment(PlanComment comment) {
        likeRepository.deleteByCommentId(comment.getId());
        mentionRepository.deleteByCommentId(comment.getId());
        commentRepository.deleteById(comment.getId());
    }

    @Transactional
    public CommentClientResponse toggleLike(Long userId, Long commentId) {
        PlanComment comment = reader.findComment(commentId);
        reader.findUser(userId);

        Optional<CommentLike> existingLike = likeRepository.findByUserIdAndCommentId(userId, commentId);
        if (existingLike.isPresent()) {
            comment.decreaseLikeCount();
            likeRepository.delete(existingLike.get());

            boolean likedByMe = likeRepository.existsByUserIdAndCommentId(userId, comment.getId());
            List<User> mentionedUsers = findMentionedUsers(comment.getId());
            return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
        }

        comment.increaseLikeCount();
        CommentLike like = CommentLike.builder()
                .userId(userId)
                .commentId(commentId)
                .build();
        likeRepository.save(like);

        boolean likedByMe = likeRepository.existsByUserIdAndCommentId(userId, commentId);
        List<User> mentionedUsers = findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    private List<User> findMentionedUsers(Long commentId) {
        return mentionRepository
                .findCommentMentionByCommentId(commentId)
                .stream()
                .map(CommentMention::getUserId)
                .map(reader::findUser)
                .toList();
    }

    @Transactional
    public void commentReport(Long userId, CommentReportRequest request) {

//        같은 신고라도 사유가 다를 수 있으니 중복 신고를 허용
//        commentReportRepository.findByReporterIdAndCommentId(userId, request.commentId()).orElseThrow(
//                () -> new ResourceNotFoundException(CURRENT_REPORT)
//        );

        commentReportRepository.save(CommentReport.builder()
                .reason(request.reason())
                .commentId(request.commentId())
                .reporterId(userId)
                .build()
        );
    }
}
