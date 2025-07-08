package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.CursorException;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.request.meet.comment.CommentCreateRequest;
import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.dto.response.meet.comment.CommentUpdateResponse;
import com.groupMeeting.dto.response.pagination.CursorPageResponse;
import com.groupMeeting.dto.response.pagination.CursorPage;
import com.groupMeeting.entity.meet.comment.CommentMention;
import com.groupMeeting.entity.meet.comment.CommentReport;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.global.event.data.notify.NotifyEventPublisher;
import com.groupMeeting.global.utils.cursor.CursorUtils;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.comment.CommentReportRepository;
import com.groupMeeting.meet.repository.comment.PlanCommentRepository;
import com.groupMeeting.dto.request.meet.comment.CommentReportRequest;

import com.groupMeeting.meet.repository.impl.comment.CommentRepositorySupport;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;
import com.groupMeeting.notification.reader.NotificationUserReader;
import lombok.RequiredArgsConstructor;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.groupMeeting.dto.client.CommentClientResponse.*;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PlanCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentReportRepository commentReportRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;

    private final ApplicationEventPublisher publisher;
    private final NotificationUserReader userReader;
    private final EntityReader reader;

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentList(Long postId, String cursor, int size) {
        List<CommentResponse> commentResponses = getComments(postId, cursor, size);
        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getComments(Long postId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return commentRepositorySupport.findCommentFirstPage(postId, size);
        }

        Long cursor = CursorUtils.decode(encodedCursor);
        if (commentRepositorySupport.validCursor(cursor)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }

        return commentRepositorySupport.findCommentNextPage(postId, cursor, size);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentReplyList(Long postId, Long commentId, String cursor, int size) {
        List<CommentResponse> commentResponses = getCommentReplies(postId, commentId, cursor, size);

        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getCommentReplies(Long postId, Long commentId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return commentRepositorySupport.findCommentReplyFirstPage(postId, commentId, size);
        }

        Long cursor = CursorUtils.decode(encodedCursor);
        if (commentRepositorySupport.validCursor(cursor)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }

        return commentRepositorySupport.findCommentReplyNextPage(postId, commentId, cursor, size);
    }

    @NotNull
    private static CursorPageResponse<CommentClientResponse> buildCursorPage(int size, List<CommentResponse> commentResponses) {
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

        PlanComment comment = PlanComment.ofParent(
                request.contents(),
                postId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        addMentions(request.mentions(), comment);
        commentRepository.save(comment);

        publishMentionEvent(postId, request.mentions(), comment);

        return ofComment(new CommentResponse(comment));
    }

    @Transactional
    public CommentClientResponse createCommentReply(Long userId, Long postId, Long parentCommentId, CommentCreateRequest request) {
        User writer = reader.findUser(userId);

        PlanComment parentComment = validateParentComment(parentCommentId);

        PlanComment comment = PlanComment.ofChild(
                request.contents(),
                postId,
                parentCommentId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        addMentions(request.mentions(), comment);
        commentRepository.save(comment);

        publishMentionEvent(postId, request.mentions(), comment);
        publishReplyEvent(userId, request.mentions(), comment, parentComment);

        return ofComment(new CommentResponse(comment));
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

        if (comment.matchWriter(user.getId())) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        comment.updateContent(request.contents());
        updateMentions(request.mentions(), comment);

        commentRepository.save(comment);

        publishMentionEvent(comment.getPostId(), request.mentions(), comment);

        return ofUpdate(new CommentUpdateResponse(comment));
    }

    private void addMentions(List<Long> mentions, PlanComment comment) {
        if (mentions == null) return;

        for (Long id : mentions) {
            User mentionedUser = reader.findUser(id);

            CommentMention mention = CommentMention.builder()
                    .mentionedUser(mentionedUser)
                    .build();

            comment.addMention(mention);
        }
    }

    private void updateMentions(List<Long> mentions, PlanComment comment) {
        comment.getMentions().clear();
        addMentions(mentions, comment);
    }

    private void publishMentionEvent(Long postId, List<Long> mentions, PlanComment comment) {
        if (mentions != null && !mentions.isEmpty()) {

            publisher.publishEvent(
                    NotifyEventPublisher.commentMention(
                            Map.of(
                                    "postId", postId.toString(),
                                    "postName", getPostName(comment.getPostId()),
                                    "userName", comment.getWriter().getNickname(),
                                    "userId", comment.getWriter().getId().toString(),
                                    "commentId", comment.getId().toString()
                            ),
                            Map.of(
                                    "commentId", comment.getId().toString()
                            )
                    )
            );
        }
    }

    private void publishReplyEvent(Long userId, List<Long> mentions, PlanComment comment, PlanComment parentComment) {
        boolean parentIsMentioned = false;

        if (mentions != null && !mentions.isEmpty()) {
            List<User> mentionedUsers = userReader.findMentionedUsers(userId, comment.getId());
            User parentCommentWriter = parentComment.getWriter();

            parentIsMentioned = mentionedUsers.stream().anyMatch(user -> user.getId().equals(parentCommentWriter.getId()));
        }

        if (!parentIsMentioned) {
            publisher.publishEvent(
                    NotifyEventPublisher.commentReply(
                            Map.of(
                                    "postId", comment.getPostId().toString(),
                                    "postName", getPostName(comment.getPostId()),
                                    "userName", comment.getWriter().getNickname(),
                                    "userId", comment.getWriter().getId().toString(),
                                    "commentId", comment.getId().toString()
                            ),
                            Map.of(
                                    "commentId", comment.getId().toString()
                            )
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
        PlanComment comment = reader.findComment(commentId);

        if (comment.matchWriter(userId)) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        commentRepository.deleteById(commentId);
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
