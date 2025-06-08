package com.groupMeeting.policy.repository;

import com.groupMeeting.entity.policy.ApiVersionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiVersionPolicyRepository extends JpaRepository<ApiVersionPolicy, Long> {
}
