package com.mople.user.repository;

import com.mople.entity.user.User;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select count(u.id) = 1 from User u where u.nickname = :nickname and u.status = :status")
    Boolean existsByNickname(@Param("nickname") String nickname, Status status);

    List<User> findByIdInAndStatus(List<Long> ids, Status status);

    @Query("select u from User u where u.email = :email and u.status = :status")
    Optional<User> loginCheck(@Param("email") String email, Status status);

    Optional<User> findByIdAndStatus(Long id, Status status);
}
