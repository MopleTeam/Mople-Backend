package com.mople.meet.service.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.CommentClientResponse;
import com.mople.dto.client.MentionClientResponse;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.response.meet.comment.CommentResponse;
import com.mople.dto.response.meet.comment.CommentUpdateResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.CursorPage;
import com.mople.entity.meet.comment.CommentReport;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentReportRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.dto.request.meet.comment.CommentReportRequest;

import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.mople.dto.client.CommentClientResponse.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PlanCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentReportRepository commentReportRepository;

    private final EntityReader reader;

    private final CommentValidator commentValidator;
    private final CommentEventPublisher commentEventPublisher;

    private final CommentMentionService mentionService;
    private final CommentLikeService likeService;
    private final CommentAutoCompleteService autoCompleteService;

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentList(Long userId, Long postId, String cursor, int size) {
        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        List<CommentResponse> commentResponses = getComments(userId, postId, cursor, size);
        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getComments(Long userId, Long postId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            List<PlanComment> commentFirstPage = commentRepositorySupport.findCommentFirstPage(postId, size);
            return getResponseAddedLikedByMe(userId, commentFirstPage);
        }

        Long cursor = Long.valueOf(CursorUtils.decode(encodedCursor));
        commentValidator.validateCursor(cursor);

        List<PlanComment> commentNextPage = commentRepositorySupport.findCommentNextPage(postId, cursor, size);
        return getResponseAddedLikedByMe(userId, commentNextPage);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentReplyList(Long userId, Long postId, Long commentId, String cursor, int size) {
        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        List<CommentResponse> commentResponses = getCommentReplies(userId, postId, commentId, cursor, size);
        return buildCursorPage(size, commentResponses);
    }

    private List<CommentResponse> getCommentReplies(Long userId, Long postId, Long commentId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            List<PlanComment> commentReplyFirstPage = commentRepositorySupport.findCommentReplyFirstPage(postId, commentId, size);
            return getResponseAddedLikedByMe(userId, commentReplyFirstPage);
        }

        Long cursor = Long.valueOf(CursorUtils.decode(encodedCursor));
        commentValidator.validateCursor(cursor);

        List<PlanComment> commentReplyNextPage = commentRepositorySupport.findCommentReplyNextPage(postId, commentId, cursor, size);
        return getResponseAddedLikedByMe(userId, commentReplyNextPage);
    }

    private List<CommentResponse> getResponseAddedLikedByMe(Long userId, List<PlanComment> comments) {
        List<Long> commentIds = comments.stream()
                .map(PlanComment::getId)
                .toList();

        List<Long> likedCommentIds = likeService.findLikedCommentIds(userId, commentIds);

        return comments.stream()
                .map(comment -> new CommentResponse(
                        comment,
                        mentionService.findMentionedUsers(comment.getId()),
                        likedCommentIds.contains(comment.getId())
                ))
                .toList();
    }

    private CursorPageResponse<CommentClientResponse> buildCursorPage(int size, List<CommentResponse> commentResponses) {
        boolean hasNext = commentResponses.size() > size;
        commentResponses = hasNext ? commentResponses.subList(0, size) : commentResponses;

        String nextCursor = hasNext && !commentResponses.isEmpty()
                ? CursorUtils.encode(commentResponses.get(commentResponses.size() - 1).commentId().toString())
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

        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);

        PlanComment comment = PlanComment.ofParent(
                request.contents(),
                postId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        commentRepository.save(comment);
        mentionService.createMentions(request.mentions(), comment.getId());

        String postName = getPostName(comment.getPostId());
        commentEventPublisher.publishMentionEvent(null, request.mentions(), comment, postName);

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    @Transactional
    public CommentClientResponse createCommentReply(Long userId, Long postId, Long parentCommentId, CommentCreateRequest request) {
        User writer = reader.findUser(userId);

        commentValidator.validatePostId(postId);
        commentValidator.validateMember(userId, postId);
        commentValidator.validateParentComment(parentCommentId);

        PlanComment parentComment = reader.findComment(parentCommentId);

        PlanComment comment = PlanComment.ofChild(
                request.contents(),
                postId,
                parentCommentId,
                LocalDateTime.now(),
                Status.ACTIVE,
                writer
        );
        commentRepository.save(comment);
        mentionService.createMentions(request.mentions(), comment.getId());

        parentComment.increaseReplyCount();

        String postName = getPostName(comment.getPostId());
        commentEventPublisher.publishMentionEvent(null, request.mentions(), comment, postName);
        commentEventPublisher.publishReplyEvent(request.mentions(), comment, parentComment, postName);

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    @Transactional
    public CommentClientResponse updateComment(Long userId, Long commentId, CommentCreateRequest request) {
        PlanComment comment = reader.findComment(commentId);
        User user = reader.findUser(userId);

        commentValidator.validateWriter(comment, user);

        List<Long> originMentions = mentionService.findUserIdByCommentId(comment.getId());

        comment.updateContent(request.contents());
        mentionService.updateMentions(request.mentions(), comment.getId());

        String postName = getPostName(comment.getPostId());
        commentEventPublisher.publishMentionEvent(originMentions, request.mentions(), comment, postName);

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        return ofUpdate(new CommentUpdateResponse(comment, mentionedUsers, likedByMe));
    }

    private String getPostName(Long postId) {
        try {
            return reader.findPlan(postId).getName();
        } catch (ResourceNotFoundException e) {
            return reader.findReviewByPostId(postId).getName();
        }
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = reader.findUser(userId);
        PlanComment comment = reader.findComment(commentId);

        commentValidator.validateWriter(comment, user);

        if (comment.isChildComment()) {
            PlanComment parentComment = reader.findComment(comment.getParentId());

            deleteSingleComment(comment.getId());
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
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        return ofComment(new CommentResponse(comment, mentionedUsers, likedByMe));
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<MentionClientResponse> searchMeetMember(Long userId, Long postId, String keyword, String cursor, int size) {
        reader.findUser(userId);
        commentValidator.validatePostId(postId);

        return autoCompleteService.getMeetMembers(postId, keyword, cursor, size);
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
