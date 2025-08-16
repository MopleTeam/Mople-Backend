package com.mople.policy.repository;

import com.mople.entity.policy.ApiVersionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiVersionPolicyRepository extends JpaRepository<ApiVersionPolicy, Long> {
}
