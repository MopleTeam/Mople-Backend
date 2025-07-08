package com.groupMeeting.meet.reader;

import com.groupMeeting.core.exception.custom.AuthException;
import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.response.meet.MeetListFindMemberResponse;
import com.groupMeeting.entity.meet.Meet;
import com.groupMeeting.entity.meet.comment.PlanComment;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.review.PlanReview;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.meet.repository.MeetRepository;
import com.groupMeeting.meet.repository.comment.PlanCommentRepository;
import com.groupMeeting.meet.repository.impl.MeetRepositorySupport;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;
import com.groupMeeting.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

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

    public List<MeetListFindMemberResponse> findMeetListUseMember(Long userId) {
        return meetRepositorySupport.findMeetUseMember(userId);
    }

    public PlanComment findComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new ResourceNotFoundException(NOT_FOUND_COMMENT)
        );
    }
}
