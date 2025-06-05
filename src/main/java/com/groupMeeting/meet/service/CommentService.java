package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.CursorException;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.dto.response.pagination.CursorPageResponse;
import com.groupMeeting.dto.response.pagination.CursorPage;
import com.groupMeeting.entity.meet.comment.CommentReport;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.global.utils.cursor.CursorUtils;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.comment.CommentReportRepository;
import com.groupMeeting.meet.repository.comment.PlanCommentRepository;
import com.groupMeeting.dto.request.meet.comment.CommentReportRequest;

import com.groupMeeting.meet.repository.impl.comment.CommentRepositorySupport;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.groupMeeting.dto.client.CommentClientResponse.ofComment;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PlanCommentRepository commentRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final CommentReportRepository commentReportRepository;
    private final EntityReader reader;

    @Transactional(readOnly = true)
    public CursorPageResponse<CommentClientResponse> getCommentList(Long postId, String cursor, int size) {
        List<CommentResponse> commentResponses = getComments(postId, cursor, size);

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

        return CursorPageResponse.of(CommentClientResponse.ofComments(commentResponses), page);
    }

    @Transactional
    public CommentClientResponse createComment(Long userId, Long postId, String content) {
        User user = reader.findUser(userId);

        PlanComment comment = commentRepository.save(
                PlanComment.builder()
                        .postId(postId)
                        .content(content)
                        .writeTime(LocalDateTime.now())
                        .status(Status.ACTIVE)
                        .writerId(userId)
                        .writerNickname(user.getNickname())
                        .writerImg(user.getProfileImg())
                        .build()
        );

        return ofComment(new CommentResponse(comment, user.getNickname(), user.getProfileImg()));
    }

    @Transactional
    public CommentClientResponse updateComment(Long userId, Long postId, Long commentId, String content) {
        PlanComment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );

        User user = reader.findUser(userId);

        if (comment.matchWriter(user.getId())) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        comment.updateContent(content);

        return ofComment(new CommentResponse(comment, user.getNickname(), user.getProfileImg()));
    }

    @Transactional
    public void deleteMeetingPlanComment(Long userId, Long commentId) {
        PlanComment comment = findComment(commentId);

        if (comment.matchWriter(userId)) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        commentRepository.deleteById(commentId);
    }

    private List<CommentResponse> getComments(Long postId, String encodedCursor, int size) {
        if (encodedCursor == null || encodedCursor.isEmpty()) {
            return commentRepositorySupport.findFirstPage(postId, size);
        }

        Long cursor = CursorUtils.decode(encodedCursor);
        if (!commentRepositorySupport.isValidCursor(cursor)) {
            throw new CursorException(NOT_FOUND_CURSOR);
        }

        return commentRepositorySupport.findNextPage(postId, cursor, size);
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

    private PlanComment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );
    }
}
