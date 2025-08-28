package com.mople.meet.repository;

import com.mople.entity.meet.Meet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetRepository extends JpaRepository<Meet, Long> {
}
