package com.groupMeeting.policy.repository;

import com.groupMeeting.entity.policy.ForceUpdatePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForceUpdatePolicyRepository extends JpaRepository<ForceUpdatePolicy, Long> {

    @Query("SELECT f FROM ForceUpdatePolicy f WHERE f.os = :os")
    Optional<ForceUpdatePolicy> findByOs(String os);
}
