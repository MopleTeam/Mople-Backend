package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.entity.meet.comment.CommentReport;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.comment.CommentReportRepository;
import com.groupMeeting.meet.repository.comment.PlanCommentRepository;
import com.groupMeeting.dto.request.meet.comment.CommentReportRequest;

import com.groupMeeting.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.groupMeeting.dto.client.CommentClientResponse.*;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final PlanCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentReportRepository commentReportRepository;
    private final EntityReader reader;

    @Transactional(readOnly = true)
    public Slice<CommentClientResponse> getCommentList(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return ofComments(getComments(postId, pageable));
    }

    @Transactional
    public Slice<CommentClientResponse> createComment(Long userId, Long postId, String content, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        User user = reader.findUser(userId);

        commentRepository.save(
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

        return ofComments(getComments(postId, pageable));
    }

    @Transactional
    public Slice<CommentClientResponse> updateComment(Long userId, Long postId, Long commentId, String content, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        PlanComment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );

        if (comment.matchWriter(userId)) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        comment.updateContent(content);

        return ofComments(getComments(postId, pageable));
    }

    @Transactional
    public void deleteMeetingPlanComment(Long userId, Long commentId) {
        PlanComment comment = findComment(commentId);

        if (comment.matchWriter(userId)) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        commentRepository.deleteById(commentId);
    }

    private Slice<CommentResponse> getComments(Long postId, Pageable pageable) {
        return commentRepository.getComment(postId, pageable).map(comment -> {
                    User user = userRepository.findById(comment.getWriterId())
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MEMBER));

                    return new CommentResponse(comment, user.getNickname(), user.getProfileImg());
                }
        );
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
