package com.groupMeeting.version.repository;

import com.groupMeeting.entity.version.ApiVersionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiVersionPolicyRepository extends JpaRepository<ApiVersionPolicy, Long> {
}
