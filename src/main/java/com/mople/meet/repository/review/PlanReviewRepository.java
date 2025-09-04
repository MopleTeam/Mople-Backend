package com.mople.meet.repository.review;

import com.mople.entity.meet.review.PlanReview;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanReviewRepository extends JpaRepository<PlanReview, Long> {

    @Query("select r from PlanReview r where r.planId = :postId")
    Optional<PlanReview> findReviewByPostId(Long postId);

    @Query("select r from PlanReview r where r.planId in :postIds and r.status = :status")
    List<PlanReview> findReviewsByPostIdIn(List<Long> postIds, Status status);

    @Modifying(clearAutomatically = true)
    @Query("""
        update PlanReview r
           set r.upload  = true,
               r.version = r.version + 1
         where r.id = :reviewId
           and r.upload = false
    """)
    int uploadedAtFirst(Long reviewId);

    @Modifying(clearAutomatically = true)
    @Query("update PlanReview r set r.status = :status, r.deletedAt = now(), r.deletedBy = :userId where r.id = :reviewId and r.status <> :status")
    int softDelete(Status status, Long reviewId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update PlanReview r set r.status = :status, r.deletedAt = now(), r.deletedBy = :userId where r.id in :reviewIds and r.status <> :status")
    int softDeleteAll(Status status, List<Long> reviewIds, Long userId);

    @Query("select r.planId from PlanReview r where r.id = :reviewId")
    Long findPlanIdById(Long reviewId);

    @Query("select r.status from PlanReview r where r.id = :reviewId")
    Status findStatusById(Long reviewId);

    Integer countByMeetIdAndStatus(Long meetId, Status status);

    boolean existsByPlanIdAndStatus(Long planId, Status status);

    Optional<PlanReview> findByPlanIdAndStatus(Long planId, Status status);

    Optional<PlanReview> findByIdAndStatus(Long id, Status status);

    List<Long> findIdsByMeetIdAndCreatorIdAndStatus(Long meetId, Long creatorId, Status status);

    List<Long> findIdsByMeetIdAndStatus(Long meetId, Status status);
}
