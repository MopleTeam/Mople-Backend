package com.mople.meet.repository;

import com.mople.entity.meet.MeetMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetMemberRepository extends JpaRepository<MeetMember, Long> {

    boolean existsByMeetIdAndUserId(Long meetId, Long userId);

    @Query("select m.id from MeetMember m where m.userId = :userId")
    List<Long> findMeetIdsByUserId(Long userId);

    void deleteByMeetId(Long meetId);

    void deleteByUserId(Long userId);
}
