package com.mople.meet.service.comment;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.client.comment.PostCommentClientResponse;
import com.mople.dto.event.data.domain.comment.CommentCreatedEvent;
import com.mople.dto.event.data.domain.comment.CommentMentionAddedEvent;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.response.meet.comment.PostCommentResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.entity.meet.comment.CommentStats;
import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.user.User;
import com.mople.global.enums.CommentTarget;
import com.mople.global.enums.Status;
import com.mople.global.utils.cursor.CursorUtils;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.comment.CommentStatsRepository;
import com.mople.meet.repository.comment.MeetCommentRepository;

import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mople.dto.client.comment.PostCommentClientResponse.*;
import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_COMMENT_STATS;
import static com.mople.global.enums.event.AggregateType.*;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.utils.cursor.CursorUtils.buildCursorPage;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private static final int COMMENT_CURSOR_FIELD_COUNT = 1;

    private final MeetPlanRepository planRepository;
    private final UserRepository userRepository;
    private final MeetCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentStatsRepository statsRepository;
    private final EntityReader reader;
    private final CommentValidator commentValidator;

    private final CommentMentionService mentionService;
    private final CommentLikeService likeService;
    private final OutboxService outboxService;

    @Transactional(readOnly = true)
    public FlatCursorPageResponse<PostCommentClientResponse> getPostCommentList(Long userId, Long postId, CursorPageRequest request) {
        commentValidator.validatePostId(postId);

        Long meetId = getMeetId(postId);
        commentValidator.validateMember(userId, meetId);

        int size = request.getSafeSize();
        List<PostCommentResponse> postCommentResponse = getComments(userId, postId, request.cursor(), size);

        return FlatCursorPageResponse.of(
                commentRepositorySupport.countParentComments(CommentTarget.POST, postId),
                buildCommentCursorPage(size, postCommentResponse)
        );
    }

    private List<PostCommentResponse> getComments(Long userId, Long postId, String encodedCursor, int size) {

        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, COMMENT_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            commentValidator.validateCursor(cursorId);
        }

        List<MeetComment> commentPage = commentRepositorySupport.findCommentPage(CommentTarget.POST, postId, cursorId, size);
        return mapToResponsesWithLikedByMe(userId, commentPage);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostCommentClientResponse> getPostCommentReplyList(Long userId, Long postId, Long commentId, CursorPageRequest request) {
        commentValidator.validatePostId(postId);

        Long meetId = getMeetId(postId);
        commentValidator.validateMember(userId, meetId);
        reader.findComment(commentId);

        int size = request.getSafeSize();
        List<PostCommentResponse> postCommentRespons = getCommentReplies(userId, postId, commentId, request.cursor(), size);

        return buildCommentCursorPage(size, postCommentRespons);
    }

    private CursorPageResponse<PostCommentClientResponse> buildCommentCursorPage(int size, List<PostCommentResponse> postCommentRespons) {
        return buildCursorPage(
                postCommentRespons,
                size,
                c -> new String[]{
                        c.commentId().toString()
                },
                PostCommentClientResponse::ofPostComments
        );
    }

    private List<PostCommentResponse> getCommentReplies(Long userId, Long postId, Long commentId, String encodedCursor, int size) {
        Long cursorId = null;

        if (encodedCursor != null && !encodedCursor.isEmpty()) {
            String[] decodeParts = CursorUtils.decode(encodedCursor, COMMENT_CURSOR_FIELD_COUNT);
            cursorId = Long.valueOf(decodeParts[0]);

            commentValidator.validateCursor(cursorId);
        }

        List<MeetComment> commentReplyPage = commentRepositorySupport.findCommentReplyPage(CommentTarget.POST, postId, commentId, cursorId, size);
        return mapToResponsesWithLikedByMe(userId, commentReplyPage);
    }

    private List<PostCommentResponse> mapToResponsesWithLikedByMe(Long userId, List<MeetComment> comments) {
        List<Long> commentIds = comments.stream()
                .map(MeetComment::getId)
                .toList();

        List<Long> writerIds = comments.stream()
                .map(MeetComment::getWriterId)
                .distinct()
                .toList();

        Map<Long, CommentStats> statsMap = statsRepository.findAllById(commentIds).stream()
                .collect(Collectors.toMap(CommentStats::getCommentId, Function.identity()));

        Map<Long, User> userMap = userRepository.findAllById(writerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, List<User>> mentionsMap = mentionService.findMentionedUsersInBatch(commentIds);

        List<Long> likedCommentIds = likeService.findLikedCommentIds(userId, commentIds);

        return comments.stream()
                .map(comment -> new PostCommentResponse(
                        comment,
                        statsMap.get(comment.getId()),
                        userMap.get(comment.getWriterId()),
                        mentionsMap.getOrDefault(comment.getId(), List.of()),
                        likedCommentIds.contains(comment.getId())
                ))
                .toList();
    }

    @Transactional
    public PostCommentClientResponse createPostComment(Long userId, Long postId, CommentCreateRequest request) {
        reader.findUser(userId);

        commentValidator.validatePostId(postId);
        Long meetId = getMeetId(postId);
        commentValidator.validateMember(userId, meetId);

        MeetComment comment = MeetComment.ofParent(
                request.contents(),
                postId,
                LocalDateTime.now(),
                userId
        );
        commentRepository.save(comment);
        statsRepository.save(CommentStats.ofParent(comment.getId()));
        mentionService.createMentions(request.mentions(), comment.getId());

        Boolean isExistMention = request.mentions() != null && !request.mentions().isEmpty();
        CommentCreatedEvent createdEvent = CommentCreatedEvent.builder()
                .postId(postId)
                .commentId(comment.getId())
                .commentWriterId(comment.getWriterId())
                .isExistMention(isExistMention)
                .parentId(comment.getParentId())
                .build();

        outboxService.save(COMMENT_CREATED, COMMENT, comment.getId(), createdEvent);

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());

        return getCommentClientResponse(comment, likedByMe);
    }

    private PostCommentClientResponse getCommentClientResponse(MeetComment comment, boolean likedByMe) {
        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());

        CommentStats stats = statsRepository.findById(comment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

        User writer = reader.findUser(comment.getWriterId());

        return ofPostComment(new PostCommentResponse(comment, stats, writer, mentionedUsers, likedByMe));
    }

    @Transactional
    public PostCommentClientResponse createPostCommentReply(Long userId, Long postId, Long parentCommentId, CommentCreateRequest request) {
        reader.findUser(userId);

        commentValidator.validatePostId(postId);

        Long meetId = getMeetId(postId);
        commentValidator.validateMember(userId, meetId);
        commentValidator.validateParentComment(parentCommentId, postId);

        MeetComment parentComment = reader.findComment(parentCommentId);

        MeetComment comment = MeetComment.ofChild(
                request.contents(),
                postId,
                parentCommentId,
                LocalDateTime.now(),
                userId
        );
        commentRepository.save(comment);
        statsRepository.save(CommentStats.ofChild(comment.getId()));

        mentionService.createMentions(request.mentions(), comment.getId());
        statsRepository.increaseReplyCount(parentComment.getId());

        Boolean isExistMention = request.mentions() != null && !request.mentions().isEmpty();
        CommentCreatedEvent createdEvent = CommentCreatedEvent.builder()
                .postId(postId)
                .commentId(comment.getId())
                .commentWriterId(comment.getWriterId())
                .isExistMention(isExistMention)
                .parentId(comment.getParentId())
                .build();

        outboxService.save(COMMENT_CREATED, COMMENT, comment.getId(), createdEvent);

        boolean likedByMe = likeService.likedByMe(userId, comment.getId());

        return getCommentClientResponse(comment, likedByMe);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PostCommentClientResponse handlePostCommentMentions(
            User user,
            MeetComment comment,
            List<Long> mentions
    ) {
        List<Long> originMentions = mentionService.findUserIdByCommentId(comment.getId());
        mentionService.updateMentions(mentions, comment.getId());

        if (mentions != null && !mentions.isEmpty()) {
            CommentMentionAddedEvent addedEvent = CommentMentionAddedEvent.builder()
                    .postId(comment.getTargetId())
                    .commentId(comment.getId())
                    .commentWriterId(comment.getWriterId())
                    .originMentions(originMentions)
                    .parentId(comment.getParentId())
                    .build();

            outboxService.save(COMMENT_MENTION_ADDED, COMMENT, comment.getId(), addedEvent);
        }

        return getCommentUpdateClientResponse(user, comment);
    }

    private PostCommentClientResponse getCommentUpdateClientResponse(User user, MeetComment comment) {
        CommentStats stats = statsRepository.findById(comment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

        List<User> mentionedUsers = mentionService.findMentionedUsers(comment.getId());
        boolean likedByMe = likeService.likedByMe(user.getId(), comment.getId());

        return ofPostComment(new PostCommentResponse(comment, stats, user, mentionedUsers, likedByMe));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<Long> preparePostCommentDeletion(MeetComment comment) {
        if (comment.isChildComment()) {
            MeetComment parentComment = reader.findComment(comment.getParentId());
            CommentStats stats = statsRepository.findById(parentComment.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT_STATS));

            if (stats.canDecreaseReplyCount()) {
                statsRepository.decreaseReplyCount(parentComment.getId());
            }

            return null;
        }

        return commentRepository.findChildIds(comment.getId());
    }

    @Transactional
    public PostCommentClientResponse toggleLike(Long userId, Long commentId) {
        MeetComment comment = reader.findComment(commentId);
        reader.findUser(userId);

        boolean likedByMe = likeService.toggleLike(userId, comment);
        MeetComment updatedComment = reader.findComment(commentId);

        return getCommentClientResponse(updatedComment, likedByMe);
    }

    private Long getMeetId(Long postId) {
        boolean existsInPlan = planRepository.existsByIdAndStatus(postId, Status.ACTIVE);

        if (existsInPlan) {
            return reader.findPlan(postId).getMeetId();
        }
        return reader.findReviewByPostId(postId).getMeetId();
    }
}
