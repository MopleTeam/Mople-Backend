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
}
