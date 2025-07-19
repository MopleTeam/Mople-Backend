package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanReport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlanReportRepository extends JpaRepository<PlanReport, Long> {
    @Query("select p from PlanReport p")
    List<PlanReport> allPlanReport();
}
