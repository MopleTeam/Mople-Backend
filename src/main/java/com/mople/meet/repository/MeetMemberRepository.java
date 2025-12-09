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

    // 모임 삭제 시 removeMeetAsCreator() 키 무효화에서 사용 - 삭제 금지
    @Query("select m.userId from MeetMember m where m.meetId = :meetId")
    List<Long> findUserIdsByMeetId(Long meetId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update MeetMember m " +
            "   set m.nicknameLower = :lower, " +
            "       m.nicknameTypeOrder = :typeOrder " +
            " where m.userId = :userId"
    )
    void updateNickname(Long userId, String lower, Integer typeOrder);

    @Query("select m from MeetMember m where m.meetId = :meetId and m.userId = :userId ")
    MeetMember findMeetIdAndUserId(Long meetId, Long userId);
}
