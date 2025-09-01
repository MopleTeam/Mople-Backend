package com.mople.meet.service.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.CommentClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.comment.CommentResponse;
import com.mople.dto.response.meet.comment.CommentUpdateResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.comment.CommentReport;
import com.mople.entity.meet.comment.CommentStats;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentReportRepository;
import com.mople.meet.repository.comment.CommentStatsRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.dto.request.meet.comment.CommentReportRequest;

import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.mople.dto.client.CommentClientResponse.*;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_COMMENT_STATS;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int COMMENT_CURSOR_FIELD_COUNT = 1;

    private final PlanCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentReportRepository commentReportRepository;
    private final CommentStatsRepository statsRepository;

    private final EntityReader reader;

    private final CommentValidator commentValidator;
    private final CommentEventPublisher commentEventPublisher;

    private final CommentMentionService mentionService;
    private final CommentLikeService likeService;
    private final CommentAutoCompleteService autoCompleteService;

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<CommentClientResponse> getCommentList(Long userId, Long postId, CursorPageRequest request) {
        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        int size = request.getSafeSize();
        List<CommentResponse> commentResponses = getComments(userId, postId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                commentRepositorySupport.countComments(postId),
                buildCommentCursorPage(size, commentResponses)
        );
    }

    private List<CommentResponse> getComments(Long userId, Long postId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, COMMENT_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            commentValidator.validateCursor(cursorId);
        }

        List<PlanComment> commentPage = commentRepositorySupport.findCommentPage(postId, cursorId, size);
        return mapToResponsesWithLikedByMe(userId, commentPage);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentReplyList(Long userId, Long postId, Long commentId, CursorPageRequest request) {
        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        int size = request.getSafeSize();
        List<CommentResponse> commentResponses = getCommentReplies(userId, postId, commentId, request.cursor(), size);

        return buildCommentCursorPage(size, commentResponses);
    }

    private CursorPageResponse<CommentClientResponse> buildCommentCursorPage(int size, List<CommentResponse> commentResponses) {
        return buildCursorPage(
                commentResponses,
                size,
                c -> new String[]{
                        c.commentId().toString()
                },
                CommentClientResponse::ofComments
        );
    }

    private List<CommentResponse> getCommentReplies(Long userId, Long postId, Long commentId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, COMMENT_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            commentValidator.validateCursor(cursorId);
        }

        List<PlanComment> commentReplyPage = commentRepositorySupport.findCommentReplyPage(postId, commentId, cursorId, size);
        return mapToResponsesWithLikedByMe(userId, commentReplyPage);
    }

    private List<CommentResponse> mapToResponsesWithLikedByMe(Long userId, List<PlanComment> comments) {
        List<Long> commentIds = comments.stream()
                .map(PlanComment::getId)
                .toList();

        List<Long> likedCommentIds = likeService.findLikedCommentIds(userId, commentIds);

        return comments.stream()
                .map(comment -> {
                    CommentStats stats = statsRepository.findById(comment.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

                    User writer = reader.findUser(comment.getWriterId());

                    return new CommentResponse(
                            comment,
                            stats,
                            writer,
                            mentionService.findMentionedUsers(comment.getId()),
                            likedCommentIds.contains(comment.getId())
                    );
                })
                .toList();
    }

    @Transactional
    public CommentClientResponse createComment(Long userId, Long postId, CommentCreateRequest request) {
        reader.findUser(userId);

        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        Meet meet = getMeet(postId);

        PlanComment comment = PlanComment.ofParent(
                request.contents(),
                postId,
                LocalDateTime.now(),
                Status.ACTIVE,
                userId
        );
        commentRepository.save(comment);
        statsRepository.save(CommentStats.ofParent(comment.getId()));

        mentionService.createMentions(request.mentions(), comment.getId());

        commentEventPublisher.publishMentionEvent(null, request.mentions(), comment, meet.getName());

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());

        return getCommentClientResponse(comment, likedByMe);
    }

    private CommentClientResponse getCommentClientResponse(PlanComment comment, boolean likedByMe) {
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        CommentStats stats = statsRepository.findById(comment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

        User writer = reader.findUser(comment.getWriterId());

        return ofComment(new CommentResponse(comment, stats, writer, mentionedUsers, likedByMe));
    }

    @Transactional
    public CommentClientResponse createCommentReply(Long userId, Long postId, Long parentCommentId, CommentCreateRequest request) {
        reader.findUser(userId);

        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);
        commentValidator.validateParentComment(parentCommentId, postId);

        PlanComment parentComment = reader.findComment(parentCommentId);
        Meet meet = getMeet(postId);

        PlanComment comment = PlanComment.ofChild(
                request.contents(),
                postId,
                parentCommentId,
                LocalDateTime.now(),
                Status.ACTIVE,
                userId
        );
        commentRepository.save(comment);
        statsRepository.save(CommentStats.ofChild(comment.getId()));

        mentionService.createMentions(request.mentions(), comment.getId());

        statsRepository.increaseReplyCount(parentComment.getId());

        commentEventPublisher.publishMentionEvent(null, request.mentions(), comment, meet.getName());
        commentEventPublisher.publishReplyEvent(request.mentions(), comment, parentComment, meet.getName());

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());

        return getCommentClientResponse(comment, likedByMe);
    }

    @Transactional
    public CommentClientResponse updateComment(Long userId, Long commentId, CommentCreateRequest request) {
        PlanComment comment = reader.findComment(commentId);
        User user = reader.findUser(userId);

        commentValidator.validateWriter(comment, user);

        List<Long> originMentions = mentionService.findUserIdByCommentId(comment.getId());

        comment.updateContent(request.contents());
        mentionService.updateMentions(request.mentions(), comment.getId());

        Meet meet = getMeet(comment.getPostId());
        commentEventPublisher.publishMentionEvent(originMentions, request.mentions(), comment, meet.getName());

        return getCommentUpdateClientResponse(userId, comment);
    }

    private CommentClientResponse getCommentUpdateClientResponse(Long userId, PlanComment comment) {
        User user = reader.findUser(userId);
        CommentStats stats = statsRepository.findById(comment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());
        boolean likedByMe = likeService.likedByMe(userId, comment.getId());

        return ofUpdate(new CommentUpdateResponse(comment, user, stats, mentionedUsers, likedByMe));
    }

    private Meet getMeet(Long postId) {
        Long meetId;
        try {
            meetId = reader.findPlan(postId).getMeetId();
        } catch (ResourceNotFoundException e) {
            meetId = reader.findReviewByPostId(postId).getMeetId();
        }

        return reader.findMeet(meetId);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = reader.findUser(userId);
        PlanComment comment = reader.findComment(commentId);

        commentValidator.validateWriter(comment, user);

        if (comment.isChildComment()) {
            PlanComment parentComment = reader.findComment(comment.getParentId());
            CommentStats stats = statsRepository.findById(parentComment.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

            if (stats.canDecreaseReplyCount()){
                deleteSingleComment(comment.getId());
                statsRepository.decreaseReplyCount(parentComment.getId());
                return;
            }
        }

        List<Long> replyIds = commentRepository
                .findAllByParentId(comment.getId())
                .stream()
                .map(PlanComment::getId)
                .toList();

        if (!replyIds.isEmpty()) {
            deleteCommentsByIds(replyIds);
        }

        deleteSingleComment(comment.getId());
    }

    private void deleteCommentsByIds(List<Long> replyIds) {
        likeService.deleteByCommentIds(replyIds);
        mentionService.deleteByCommentIds(replyIds);

        commentRepository.deleteByIdIn(replyIds);
    }

    private void deleteSingleComment(Long commentId) {
        likeService.deleteByCommentId(commentId);
        mentionService.deleteByCommentId(commentId);

        commentRepository.deleteById(commentId);
    }

    @Transactional
    public CommentClientResponse toggleLike(Long userId, Long commentId) {
        PlanComment comment = reader.findComment(commentId);
        reader.findUser(userId);

        boolean likedByMe = likeService.toggleLike(userId, comment);
        PlanComment updatedComment = reader.findComment(commentId);

        return getCommentClientResponse(updatedComment, likedByMe);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<UserRoleClientResponse> searchMeetMember(Long userId, Long postId, String keyword, CursorPageRequest request) {
        reader.findUser(userId);
        commentValidator.validatePostId(postId);

        Meet meet = getMeet(postId);

        Long meetId = meet.getId();
        Long hostId = meet.getCreatorId();
        Long creatorId = getHostId(postId);

        int size = request.getSafeSize();
        List<MeetMember> meetMembers = autoCompleteService.getMeetMembers(meetId, hostId, creatorId, keyword, request.cursor(), size);

        return autoCompleteService.buildAutoCompleteCursorPage(size, meetMembers, hostId, creatorId);
    }

    private Long getHostId(Long postId) {
        try {
            return reader.findPlan(postId).getCreatorId();
        } catch (ResourceNotFoundException e) {
            return reader.findReviewByPostId(postId).getCreatorId();
        }
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
