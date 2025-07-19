package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.PlanParticipant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PlanParticipantRepository extends JpaRepository<PlanParticipant, Long> {
    Optional<PlanParticipant> findByUserIdAndPlanId(Long userId, Long planId);

    boolean existsByPlanIdAndUserId(Long planId, Long userId);
}
