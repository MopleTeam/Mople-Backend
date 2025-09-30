package com.mople.entity.meet.plan;

import com.mople.entity.common.BaseTimeEntity;

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

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "review_id")
    private Long reviewId;

    @Builder
    public PlanParticipant(Long planId, Long userId, Long reviewId) {
        this.planId = planId;
        this.userId = userId;
        this.reviewId = reviewId;
    }

    public void updateReview(Long reviewId) {
        this.planId = null;
        this.reviewId = reviewId;
    }
}
