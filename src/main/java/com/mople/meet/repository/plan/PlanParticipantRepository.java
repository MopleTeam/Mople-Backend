package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanParticipant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanParticipantRepository extends JpaRepository<PlanParticipant, Long> {

    boolean existsByPlanIdAndUserId(Long planId, Long userId);

    Integer countByPlanId(Long planId);

    Integer countByReviewId(Long reviewId);

    List<PlanParticipant> findByPlanId(Long planId);

    void deleteByPlanId(Long planId);

    void deleteByPlanIdAndUserId(Long planId, Long userId);

    void deleteByReviewId(Long reviewId);
}
