package com.mople.meet.repository;

import com.mople.entity.meet.MeetMember;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MeetMemberRepository extends JpaRepository<MeetMember, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete MeetMember m where m.meetId = :meetId and m.userId = :userId")
    void deleteMember(Long meetId, Long userId);
}
