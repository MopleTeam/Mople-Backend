package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.MeetPlan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetPlanRepository extends JpaRepository<MeetPlan, Long>{

    @Query("select p from MeetPlan p where p.planTime < :time")
    List<MeetPlan> findPreviousPlanAll(LocalDateTime time);

    @Query("select p from MeetPlan p where p.id in :planIds")
    List<MeetPlan> findPlanAndTime(List<Long> planIds);
}
