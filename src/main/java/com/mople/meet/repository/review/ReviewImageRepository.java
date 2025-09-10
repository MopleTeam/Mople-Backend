package com.mople.meet.repository.review;

import com.mople.entity.meet.review.ReviewImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReviewId(Long id);

    @Query("select i from ReviewImage i where i.id in :id and i.reviewId = :reviewId")
    List<ReviewImage> getReviewImages(List<String> id, Long reviewId);

    @Query("select i.reviewImage from ReviewImage i where i.reviewId = :reviewId")
    List<String> findReviewImagesByReviewId(Long reviewId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from ReviewImage i " +
            "      where i.reviewId = :reviewId "
    )
    void deleteByReviewId(Long reviewId);
}
