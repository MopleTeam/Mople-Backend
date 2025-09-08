package com.mople.meet.service.comment;

import com.mople.core.exception.custom.AsyncException;
import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.ExceptionReturnCode.NOT_MEMBER;

@Component
@RequiredArgsConstructor
public class CommentValidator {

    private final MeetMemberRepository meetMemberRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final CommentRepositorySupport commentRepositorySupport;
    private final EntityReader reader;

    public void validateCursor(Long cursorId) {
        if (commentRepositorySupport.isCursorInvalid(cursorId)) {
            throw new CursorException(INVALID_CURSOR);
        }
    }

    public void validatePostId(Long postId) {
        boolean existsInPlan = planRepository.existsByIdAndStatus(postId, Status.ACTIVE);
        boolean existsInReview = reviewRepository.existsByPlanIdAndStatus(postId, Status.ACTIVE);

        if (!existsInPlan && !existsInReview) {
            throw new ResourceNotFoundException(NOT_FOUND_POST);
        }
    }

    public void validateMember(Long userId, Long meetId) {
        reader.findUser(userId);

        if (!meetMemberRepository.existsByMeetIdAndUserId(meetId, userId)) {
            throw new ResourceNotFoundException(NOT_MEMBER);
        }
    }

    public void validateParentComment(Long commentId, Long postId) {
        PlanComment parentComment = reader.findComment(commentId);

        if (!parentComment.getPostId().equals(postId)) {
            throw new ResourceNotFoundException(NOT_PARENT_COMMENT);
        }

        if (parentComment.getParentId() != null) {
            throw new ResourceNotFoundException(NOT_PARENT_COMMENT);
        }
    }

    public void validateWriter(PlanComment comment, User user, Long version) {
        if (comment.matchWriter(user.getId())) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }

        if (!comment.getVersion().equals(version)) {
            throw new AsyncException(REQUEST_CONFLICT);
        }
    }
}
