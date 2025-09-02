package com.mople.meet.repository;

import com.mople.entity.meet.Meet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetRepository extends JpaRepository<Meet, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Meet m set m.deleted = true, m.deletedAt = now(), m.deletedBy = :userId where m.id = :meetId")
    int softDelete(Long meetId, Long userId);

    @Query("select m.id from Meet m where m.creatorId = :creatorId")
    List<Long> findIdsByCreatorId(Long creatorId);
}
