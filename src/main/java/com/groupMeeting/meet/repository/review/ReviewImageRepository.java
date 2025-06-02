package com.groupMeeting.meet.repository.review;

import com.groupMeeting.entity.meet.review.ReviewImage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewId(Long id);

    @Query("select i from ReviewImage i where i.id in :id and i.review.id = :reviewId")
    List<ReviewImage> getReviewImages(List<String> id, Long reviewId);
}
