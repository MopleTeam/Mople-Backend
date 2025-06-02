package com.groupMeeting.meet.repository;

import com.groupMeeting.entity.meet.MeetMember;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MeetMemberRepository extends JpaRepository<MeetMember, Long> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("delete MeetMember m where m.joinMeet.id = :meetId and m.user.id = :userId")
    void deleteMember(Long meetId, Long userId);
}
