package com.mople.notification.repository;

import com.mople.entity.notification.FirebaseToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FirebaseTokenRepository extends JpaRepository<FirebaseToken, Long> {
    Optional<FirebaseToken> findByUserId(Long userId);

    @Query("select t.token from FirebaseToken t where t.userId in :memberIds")
    List<String> findMemberTokens(List<Long> memberIds);
}
