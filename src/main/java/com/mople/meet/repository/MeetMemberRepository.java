package com.mople.meet.repository;

import com.mople.entity.meet.MeetMember;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetMemberRepository extends JpaRepository<MeetMember, Long> {

    boolean existsByMeetIdAndUserId(Long meetId, Long userId);

    @Query("select m.meetId from MeetMember m where m.userId = :userId")
    List<Long> findMeetIdsByUserId(Long userId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from MeetMember m " +
            "      where m.meetId = :meetId "
    )
    void deleteByMeetId(Long meetId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from MeetMember m " +
            "      where m.meetId in :meetIds " +
            "        and m.userId = :userId "
    )
    void deleteByMeetIdsAndUserId(List<Long> meetIds, Long userId);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from MeetMember m " +
            "      where m.meetId = :meetId " +
            "        and m.userId = :userId "
    )
    void deleteByMeetIdAndUserId(Long meetId, Long userId);
}
