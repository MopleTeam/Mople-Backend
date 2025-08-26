package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanParticipant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanParticipantRepository extends JpaRepository<PlanParticipant, Long> {

    boolean existsByPlanIdAndUserId(Long planId, Long userId);

    List<PlanParticipant> findParticipantsByPlanId(Long planId);

    void deleteByPlanId(Long planId);
}
