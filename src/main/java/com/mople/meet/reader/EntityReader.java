package com.mople.meet.reader;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.MeetComment;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.global.enums.Status;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.comment.MeetCommentRepository;
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
    private final MeetCommentRepository commentRepository;

    public User findUser(Long userId) {
        return userRepository.findByIdAndStatus(userId, Status.ACTIVE)
                .orElseThrow(() -> new AuthException(NOT_FOUND_USER));
    }

    public Meet findMeet(Long meetId) {
        return meetRepository.findByIdAndStatus(meetId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MEET));
    }

    public MeetPlan findPlan(Long planId) {
        return planRepository.findByIdAndStatus(planId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_PLAN));
    }

    public PlanReview findReview(Long reviewId) {
        return planReviewRepository.findByIdAndStatus(reviewId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));
    }

    public PlanReview findReviewByPostId(Long postId) {
        return planReviewRepository.findByPlanIdAndStatus(postId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_REVIEW));
    }

    public MeetComment findComment(Long commentId) {
        return commentRepository.findByIdAndStatus(commentId, Status.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_COMMENT));
    }
}
