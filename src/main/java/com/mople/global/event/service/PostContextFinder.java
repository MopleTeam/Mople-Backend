package com.mople.global.event.service;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PostContextFinder {

    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final MeetRepository meetRepository;

    public PostContext resolve(Long postId) {
        Optional<MeetPlan> planOptional = planRepository.findByIdAndStatus(postId, Status.ACTIVE);

        if (planOptional.isPresent()) {
            MeetPlan plan = planOptional.get();

            Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                    .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

            return PostContext.plan(meet, plan);
        }

        PlanReview review = reviewRepository.findByPlanIdAndStatus(postId, Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        return PostContext.review(meet, review);
    }

    @Getter
    public static class PostContext {
        public enum Type {
            PLAN, REVIEW
        }

        private final Type type;
        private final Meet meet;
        private final Long planId;
        private final Long reviewId;

        private PostContext(Type type, Meet meet, Long planId, Long reviewId) {
            this.type = type;
            this.meet = meet;
            this.planId = planId;
            this.reviewId = reviewId;
        }

        public static PostContext plan(Meet meet, MeetPlan plan) {
            return new PostContext(Type.PLAN, meet, plan.getId(), null);
        }

        public static PostContext review(Meet meet, PlanReview review) {
            return new PostContext(Type.REVIEW, meet, review.getPlanId(), review.getId());
        }

        public boolean isPlan()   {
            return type == Type.PLAN;
        }

        public boolean isReview() {
            return type == Type.REVIEW;
        }
    }
}