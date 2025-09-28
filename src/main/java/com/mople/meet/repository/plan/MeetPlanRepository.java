package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.MeetPlan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetPlanRepository extends JpaRepository<MeetPlan, Long>{
    @Query("select p from MeetPlan p where p.planTime < :time")
    List<MeetPlan> findPreviousPlanAll(LocalDateTime time);

    @Query("select p from MeetPlan p join fetch p.participants join fetch p.creator where p.id = :planId")
    Optional<MeetPlan> findPlanAll(Long planId);

    @Query("select p from MeetPlan p join fetch p.meet join fetch p.participants where p.id = :planId")
    Optional<MeetPlan> findPlanAndMeet(Long planId);

    @Query("select p from MeetPlan p join fetch p.meet where p.meet.id in :meetIds")
    List<MeetPlan> findCreatedPlanAll(@Param("meetIds") List<Long> meetIds);

    @Query("select p from MeetPlan p where p.id in :planIds")
    List<MeetPlan> findPlanAndTime(List<Long> planIds);
}
