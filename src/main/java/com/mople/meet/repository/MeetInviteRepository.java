package com.mople.meet.repository;

import com.mople.entity.meet.MeetInvite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MeetInviteRepository extends JpaRepository<MeetInvite, UUID> {

    @Query("SELECT i FROM MeetInvite i where i.inviteCode = :code")
    Optional<MeetInvite> findByInviteCodeMeet(String code);

    @Modifying(flushAutomatically = true)
    @Query(
            "delete from MeetInvite i " +
            "      where i.meetId = :meetId "
    )
    void deleteByMeetId(Long meetId);
}
