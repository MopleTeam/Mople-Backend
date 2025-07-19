package com.mople.entity.meet.plan;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.user.User;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "plan_participant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanParticipant extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private MeetPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private PlanReview review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public PlanParticipant(MeetPlan plan, User user, PlanReview review) {
        this.plan = plan;
        this.user = user;
        this.review = review;
    }

    public void updatePlan(MeetPlan plan) {
        this.plan = plan;
    }

    public void updateReview(PlanReview review) {
        this.plan = null;
        this.review = review;
    }
}
