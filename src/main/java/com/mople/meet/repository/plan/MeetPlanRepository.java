package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.MeetPlan;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetPlanRepository extends JpaRepository<MeetPlan, Long>{

    @Query("select p from MeetPlan p where p.planTime < :time and p.status = :status")
    List<MeetPlan> findPreviousPlanAll(LocalDateTime time, Status status);

    @Query("select p from MeetPlan p where p.id in :planIds and p.status = :status")
    List<MeetPlan> findPlanAndTime(List<Long> planIds, Status status);

    @Modifying(clearAutomatically = true)
    @Query(
            "update MeetPlan p " +
            "   set p.temperature = :temperature, " +
            "       p.pop = :pop, " +
            "       p.weatherIcon = :icon, " +
            "       p.weatherUpdatedAt = CURRENT_TIMESTAMP " +
            " where p.id = :planId "
    )
    int updateWeather(Long planId, Double temperature, Double pop, String icon);

    @Modifying(clearAutomatically = true)
    @Query(
            "update MeetPlan p " +
            "   set p.temperature = null, " +
            "       p.pop = null, " +
            "       p.weatherIcon = null, " +
            "       p.weatherUpdatedAt = CURRENT_TIMESTAMP " +
            " where p.id = :planId "
    )
    int deleteWeather(Long planId);

    @Modifying(clearAutomatically = true)
    @Query(
            "update MeetPlan p " +
            "   set p.status = :status, " +
            "       p.deletedAt = :deletedAt, " +
            "       p.deletedBy = :userId " +
            " where p.id = :planId " +
            "   and p.status <> :status"
    )
    int softDelete(Status status, Long planId, Long userId, LocalDateTime deletedAt);

    @Modifying(clearAutomatically = true)
    @Query(
            "update MeetPlan p " +
            "   set p.status = :status, " +
            "       p.deletedAt = :deletedAt, " +
            "       p.deletedBy = :userId " +
            " where p.id in :planIds " +
            "   and p.status <> :status "
    )
    int softDeleteAll(Status status, List<Long> planIds, Long userId, LocalDateTime deletedAt);

    Integer countByMeetIdAndStatus(Long meetId, Status status);

    boolean existsByIdAndStatus(Long id, Status status);

    @Query("select p from MeetPlan p where p.id = :id and p.status = :status")
    Optional<MeetPlan> findByIdAndStatus(Long id, Status status);

    @Query(
            "select p.id " +
            "  from MeetPlan p " +
            " where p.meetId = :meetId " +
            "   and p.creatorId = :creatorId " +
            "   and p.status = com.mople.global.enums.Status.ACTIVE"
    )
    List<Long> findIdsByMeetIdAndCreatorId(Long meetId, Long creatorId);

    @Query(
            "select p.id " +
            "  from MeetPlan p " +
            " where p.meetId = :meetId " +
            "   and p.status = com.mople.global.enums.Status.ACTIVE"
    )
    List<Long> findIdsByMeetId(Long meetId);

    @Modifying(clearAutomatically = true)
    @Query(
            "delete from MeetPlan p " +
            "      where p.id = :planId " +
            "        and p.status = com.mople.global.enums.Status.DELETED"
    )
    void hardDeleteById(Long planId);
}
