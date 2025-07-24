package com.mople.meet.service.comment;

import com.mople.core.exception.custom.CursorException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.meet.reader.EntityReader;
import com.mople.meet.repository.impl.comment.CommentRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.mople.global.enums.ExceptionReturnCode.*;
import static com.mople.global.enums.ExceptionReturnCode.NOT_MEMBER;

@Component
@RequiredArgsConstructor
public class CommentValidator {

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
        boolean existsInPlan = planRepository.existsById(postId);
        boolean existsInReview = reviewRepository.findReviewByPostId(postId).isPresent();

        if (!existsInPlan && !existsInReview) {
            throw new ResourceNotFoundException(NOT_FOUND_POST);
        }
    }

    public void validateMember(Long userId, Long postId) {
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

    public void validateParentComment(Long commentId) {
        PlanComment parentComment = reader.findComment(commentId);

        if (parentComment.getParentId() != null) {
            throw new ResourceNotFoundException(NOT_PARENT_COMMENT);
        }
    }

    public void validateWriter(PlanComment comment, User user) {
        if (comment.matchWriter(user.getId())) {
            throw new ResourceNotFoundException(NOT_CREATOR);
        }
    }
}
