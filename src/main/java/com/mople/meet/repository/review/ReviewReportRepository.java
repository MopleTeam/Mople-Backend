package com.mople.meet.repository.review;

import com.mople.entity.meet.review.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    @Query("select r from ReviewReport r")
    List<ReviewReport> allReviewReport();
}
