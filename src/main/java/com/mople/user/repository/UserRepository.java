package com.mople.user.repository;

import com.mople.entity.user.User;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "select count(u.id) = 1 from User u where u.nickname = :nickname")
    Boolean existsByNickname(@Param("nickname") String nickname);

    @Query(value = "select u from User u where u.email = :email")
    Optional<User> loginCheck(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "select u from User u where u.id = :id")
    Optional<User> findByIdWithLock(@Param("id") Long id);
}
