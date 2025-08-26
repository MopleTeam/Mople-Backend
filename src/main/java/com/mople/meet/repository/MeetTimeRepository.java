package com.mople.meet.repository;

import com.mople.entity.meet.MeetTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MeetTimeRepository extends JpaRepository<MeetTime, Long> {

    @Query("select mt from MeetTime mt where mt.planId = :planId")
    Optional<MeetTime> findByPlanId(Long planId);

    void deleteByPlanId(Long planId);
}
