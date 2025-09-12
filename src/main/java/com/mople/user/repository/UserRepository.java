package com.mople.user.repository;

import com.mople.entity.user.User;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select count(u.id) = 1 from User u where u.nickname = :nickname")
    Boolean existsByNickname(@Param("nickname") String nickname);

    @Query("select u from User u where u.id in :ids and u.status = :status")
    List<User> findByIdInAndStatus(List<Long> ids, Status status);

    @Query(
            "select u " +
            "  from User u " +
            " where u.email = :email " +
            "   and u.status = com.mople.global.enums.Status.ACTIVE"
    )
    Optional<User> loginCheck(@Param("email") String email);

    @Query("select u from User u where u.id = :id and u.status = :status")
    Optional<User> findByIdAndStatus(Long id, Status status);

    @Modifying(flushAutomatically = true)
    @Query(
            "update User u " +
            "   set u.email = null, " +
            "       u.nickname = :nickname, " +
            "       u.profileImg = null, " +
            "       u.lastLaunchAt = null, " +
            "       u.status = :status," +
            "       u.socialProvider = null " +
            " where u.id = :id " +
            "   and u.version = :baseVersion" +
             "  and u.status <> :status"
    )
    int removeUser(Status status, String nickname, Long id, long baseVersion);

    @Query("select u.version from User u where u.id = :userId")
    long findVersion(Long userId);
}
