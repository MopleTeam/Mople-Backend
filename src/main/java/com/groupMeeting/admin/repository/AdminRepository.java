package com.groupMeeting.admin.repository;

import com.groupMeeting.entity.user.Admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByNameAndPw(String name, String pw);
}
