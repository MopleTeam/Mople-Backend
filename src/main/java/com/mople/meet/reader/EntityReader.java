package com.mople.meet.reader;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.BadRequestException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.ExceptionReturnCode.*;

@Component
@RequiredArgsConstructor
public class EntityReader {
    private final UserRepository userRepository;
    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository planReviewRepository;
    private final PlanCommentRepository commentRepository;

    public User findUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(NOT_USER));

        if (user.getStatus() == Status.ACTIVE) {
            return user;
        }

        throw new BadRequestException(INVALID_USER);
    }

    public Meet findMeet(Long meetId) {
        Meet meet = meetRepository.findById(meetId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MEET));

        if (meet.getStatus() == Status.ACTIVE) {
            return meet;
        }

        throw new BadRequestException(INVALID_MEET);
    }

    public MeetPlan findPlan(Long planId) {
        MeetPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_PLAN));

        if (plan.getStatus() == Status.ACTIVE) {
            return plan;
        }

        throw new BadRequestException(INVALID_PLAN);
    }

    public PlanReview findReview(Long reviewId) {
        PlanReview review = planReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        if (review.getStatus() == Status.ACTIVE) {
            return review;
        }

        throw new BadRequestException(INVALID_REVIEW);
    }

    public PlanReview findReviewByPostId(Long postId) {
        PlanReview review = planReviewRepository.findReviewByPostId(postId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));

        if (review.getStatus() == Status.ACTIVE) {
            return review;
        }

        throw new BadRequestException(INVALID_REVIEW);
    }

    public PlanComment findComment(Long commentId) {
        PlanComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT));

        if (comment.getStatus() == Status.ACTIVE) {
            return comment;
        }

        throw new BadRequestException(INVALID_COMMENT);
    }
}
