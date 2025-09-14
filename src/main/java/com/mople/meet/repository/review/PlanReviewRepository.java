package com.mople.meet.repository.review;

import com.mople.entity.meet.review.PlanReview;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlanReviewRepository extends JpaRepository<PlanReview, Long> {

    @Query("select r from PlanReview r where r.planId = :postId")
    Optional<PlanReview> findReviewByPostId(Long postId);

    @Query("select r from PlanReview r where r.planId in :postIds and r.status = :status")
    List<PlanReview> findReviewsByPostIdIn(List<Long> postIds, Status status);

    @Modifying(flushAutomatically = true)
    @Query("""
        update PlanReview r
           set r.upload  = true
         where r.id = :reviewId
           and r.upload = false
    """)
    int markUploaded(Long reviewId);

    @Modifying(flushAutomatically = true)
    @Query("""
        update PlanReview r
           set r.version = r.version + 1
         where r.id = :reviewId
    """)
    int upVersion(Long reviewId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update PlanReview r " +
            "   set r.status = :status, " +
            "       r.deletedAt = :deletedAt, " +
            "       r.deletedBy = :userId " +
            " where r.id in :reviewIds " +
            "   and r.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> reviewIds, Long userId, LocalDateTime deletedAt);

    @Query("select r.planId from PlanReview r where r.id = :reviewId")
    Long findPlanIdById(Long reviewId);

    Integer countByMeetIdAndStatus(Long meetId, Status status);

    boolean existsByPlanIdAndStatus(Long planId, Status status);

    @Query("select r from PlanReview r where r.planId = :planId and r.status = :status")
    Optional<PlanReview> findByPlanIdAndStatus(Long planId, Status status);

    @Query("select r from PlanReview r where r.id = :id and r.status = :status")
    Optional<PlanReview> findByIdAndStatus(Long id, Status status);

    @Query(
            "select r.id " +
            "  from PlanReview r " +
            " where r.meetId = :meetId " +
            "   and r.creatorId = :creatorId " +
            "   and r.status = com.mople.global.enums.Status.ACTIVE"
    )
    List<Long> findIdsByMeetIdAndCreatorId(Long meetId, Long creatorId);

    @Query(
            "select r.id " +
            "  from PlanReview r " +
            " where r.meetId = :meetId " +
            "   and r.status = com.mople.global.enums.Status.ACTIVE"
    )
    List<Long> findIdsByMeetId(Long meetId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete " +
              "from PlanReview r " +
            " where r.id = :reviewId " +
            "   and r.status = com.mople.global.enums.Status.DELETED"
    )
    void hardDeleteById(Long reviewId);

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    @Query(value = "select version from plan_review where review_id = :reviewId", nativeQuery = true)
    Long findVersion(Long reviewId);
}
