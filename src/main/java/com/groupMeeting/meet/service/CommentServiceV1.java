package com.groupMeeting.meet.service;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;
import com.groupMeeting.meet.reader.EntityReader;
import com.groupMeeting.meet.repository.comment.CommentReportRepository;
import com.groupMeeting.meet.repository.comment.PlanCommentRepository;
import com.groupMeeting.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.groupMeeting.dto.client.CommentClientResponse.ofComments;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_COMMENT;

@Service
@RequiredArgsConstructor
public class CommentServiceV1 {
    private final PlanCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CommentReportRepository commentReportRepository;
    private final EntityReader reader;

    @Transactional(readOnly = true)
    public List<CommentClientResponse> getCommentList(Long postId) {
        return ofComments(getComments(postId));
    }

    @Transactional
    public List<CommentClientResponse> createComment(Long userId, Long postId, String content) {
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

        return ofComments(getComments(postId));
    }

    @Transactional
    public List<CommentClientResponse> updateComment(Long userId, Long postId, Long commentId, String content) {
        PlanComment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );

        if (comment.matchWriter(userId)) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        comment.updateContent(content);

        return ofComments(getComments(postId));
    }

    private List<CommentResponse> getComments(Long postId) {
        return commentRepository.getComment(postId).stream().map(comment -> {
                    User user = userRepository.findById(comment.getWriterId())
                            .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MEMBER));
                    return new CommentResponse(comment, user.getNickname(), user.getProfileImg());
                }
        ).toList();
    }
}
