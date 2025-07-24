package com.mople.meet.reader;

import com.mople.core.exception.custom.AuthException;
import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.response.meet.MeetListFindMemberResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.comment.PlanComment;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.meet.repository.impl.MeetRepositorySupport;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.*;

@Component
@RequiredArgsConstructor
public class EntityReader {
    private final UserRepository userRepository;
    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final MeetRepositorySupport meetRepositorySupport;
    private final PlanReviewRepository planReviewRepository;
    private final PlanCommentRepository commentRepository;

    public User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new AuthException(NOT_USER)
        );
    }

    public Meet findMeet(Long meetId) {
        return meetRepository.findById(meetId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_MEET)
        );
    }

    public Meet findMeetAndReview(Long meetId) {
        return meetRepository.findMeetAndReviews(meetId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_MEET)
        );
    }

    public MeetPlan findPlan(Long planId) {
        return planRepository.findById(planId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_PLAN)
        );
    }

    public PlanReview findReview(Long reviewId) {
        return planReviewRepository.findById(reviewId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_REVIEW)
        );
    }

    public PlanReview findReviewByPostId(Long postId) {
        return planReviewRepository.findReviewByPostId(postId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_REVIEW)
        );
    }

    public List<MeetListFindMemberResponse> findMeetListUseMember(Long userId) {
        return meetRepositorySupport.findMeetUseMember(userId);
    }

    public PlanComment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );
    }
}
