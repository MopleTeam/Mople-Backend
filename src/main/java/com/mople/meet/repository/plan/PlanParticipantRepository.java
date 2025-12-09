package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanParticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
            "      where p.planId in :planIds " +
            "        and p.userId = :userId "
    )
    void deleteByPlanIdsAndUserId(List<Long> planIds, Long userId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from PlanParticipant p " +
            "      where p.reviewId = :reviewId "
    )
    void deleteByReviewId(Long reviewId);

    // 일정 삭제 시 deletePlan() 키 무효화에서 사용 - 삭제 금지
    @Query("select p.userId from PlanParticipant p where p.planId = :planId")
    List<Long> findUserIdsByPlanId(Long planId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update PlanParticipant p " +
            "   set p.nicknameLower = :lower, " +
            "       p.nicknameTypeOrder = :typeOrder " +
            " where p.userId = :userId"
    )
    void updateNickname(Long userId, String lower, Integer typeOrder);

    @Modifying(flushAutomatically = true)
    @Query("""
            update PlanParticipant pp
            set pp.roleOrder = case
                when (pp.planId in (select p.id from MeetPlan p where p.meetId = :meetId and p.creatorId = :oldHostId))
                  or (pp.reviewId in (select r.id from PlanReview r where r.meetId = :meetId and r.creatorId = :oldHostId))
                then 2
                else 3
            end
            where pp.userId = :oldHostId
    """)
    void updateOldHostRoles(Long meetId, Long oldHostId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update PlanParticipant pp
            set pp.roleOrder = 1
            where pp.userId = :newHostId
              and (
                  pp.planId in (select p.id from MeetPlan p where p.meetId = :meetId)
                  or pp.reviewId in (select r.id from PlanReview r where r.meetId = :meetId)
              )
    """)
    void updateNewHostRoles(Long meetId, Long newHostId);
}
