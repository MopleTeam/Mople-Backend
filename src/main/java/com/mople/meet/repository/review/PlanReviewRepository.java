package com.mople.meet.repository.review;

import com.mople.entity.meet.review.PlanReview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanReviewRepository extends JpaRepository<PlanReview, Long> {
    @Query("select r from PlanReview r where r.creatorId = :userId")
    List<PlanReview> findReviewByUserId(Long userId);

    @Query("select r from PlanReview r where r.id = :reviewId")
    Optional<PlanReview> findReview(Long reviewId);

    @Query("select r from PlanReview r where r.planId = :postId")
    Optional<PlanReview> findReviewByPostId(Long postId);

    @Query("select r from PlanReview r where r.planId in :postIds")
    List<PlanReview> findReviewsByPostId(List<Long> postIds);

    @Modifying(clearAutomatically = true)
    @Query("""
        update PlanReview r
           set r.upload  = true,
               r.version = r.version + 1
         where r.id = :reviewId
           and r.upload = false
    """)
    int uploadedAtFirst(Long reviewId);

    @Query("select r.id from PlanReview r where r.meetId = :meetId and r.creatorId = :creatorId")
    List<Long> findIdsByMeetIdAndCreatorId(Long meetId, Long creatorId);

    @Modifying(clearAutomatically = true)
    @Query("update PlanReview r set r.deleted = true, r.deletedAt = now(), r.deletedBy = :userId where r.id = :reviewId")
    int softDelete(Long reviewId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update PlanReview r set r.deleted = true, r.deletedAt = now(), r.deletedBy = :userId where r.id in :reviewIds")
    int softDeleteAll(List<Long> reviewIds, Long userId);

    @Query("select r.id from PlanReview r where r.meetId = :meetId")
    List<Long> findIdsByMeetId(Long meetId);

    @Query("select r.planId from PlanReview r where r.id = :reviewId")
    Long findPlanIdById(Long reviewId);
}
