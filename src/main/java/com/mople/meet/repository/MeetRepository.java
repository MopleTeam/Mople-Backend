package com.mople.meet.repository;

import com.mople.entity.meet.Meet;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetRepository extends JpaRepository<Meet, Long> {

    @Modifying(clearAutomatically = true)
    @Query("update Meet m " +
            "  set m.status = :status, " +
            "      m.deletedAt = :deletedAt, " +
            "      m.deletedBy = :userId " +
            "where m.id = :meetId " +
            "  and m.status <> :status"
    )
    int softDelete(Status status, Long meetId, Long userId, LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true)
    @Query("update Meet m " +
            "  set m.status = :status, " +
            "      m.deletedAt = :deletedAt, " +
            "      m.deletedBy = :userId " +
            "where m.id in :meetIds " +
            "  and m.status <> :status"
    )
    int softDeleteAll(Status status, List<Long> meetIds, Long userId, LocalDateTime deletedAt);

    @Query("select m.status from Meet m where m.id = :meetId")
    Status findStatusById(Long meetId);

    Optional<Meet> findByIdAndStatus(Long id, Status status);

    List<Long> findIdsByCreatorIdAndStatus(Long creatorId, Status status);
}
