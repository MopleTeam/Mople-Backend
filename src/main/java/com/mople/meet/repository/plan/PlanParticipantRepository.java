package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanParticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlanParticipantRepository extends JpaRepository<PlanParticipant, Long> {

    boolean existsByPlanIdAndUserId(Long planId, Long userId);

    Integer countByPlanId(Long planId);

    Integer countByReviewId(Long reviewId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update PlanParticipant p " +
            "   set p.planId = null," +
            "       p.reviewId = :reviewId" +
            " where p.planId = :planId"
    )
    int updateReviewId(Long planId, Long reviewId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from PlanParticipant p " +
            "      where p.planId = :planId "
    )
    void deleteByPlanId(Long planId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from PlanParticipant p " +
            "      where p.planId = :planId " +
            "        and p.userId = :userId "
    )
    void deleteByPlanIdAndUserId(Long planId, Long userId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from PlanParticipant p " +
            "      where p.reviewId = :reviewId "
    )
    void deleteByReviewId(Long reviewId);
}
