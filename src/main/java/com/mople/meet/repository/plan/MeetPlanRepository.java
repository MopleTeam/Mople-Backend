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
    @Query(value = """
       UPDATE meet_plan
          SET temperature = :temperature,
              pop = :pop,
              weather_icon = :icon,
              weather_update_at = now()
        WHERE id = :planId
       """, nativeQuery = true)
    int updateWeather(Long planId, Double temperature, Double pop, String icon);

    @Modifying(clearAutomatically = true)
    @Query(value = """
       UPDATE meet_plan
          SET temperature = null,
              pop = null,
              weather_icon = null,
              weather_update_at = now()
        WHERE id = :planId
       """, nativeQuery = true)
    int deleteWeather(Long planId);

    @Modifying(clearAutomatically = true)
    @Query("update MeetPlan p set p.status = :status, p.deletedAt = now(), p.deletedBy = :userId where p.id = :planId and p.status <> :status")
    int softDelete(Status status, Long planId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update MeetPlan p set p.status = :status, p.deletedAt = now(), p.deletedBy = :userId where p.id in :planIds and p.status <> :status")
    int softDeleteAll(Status status, List<Long> planIds, Long userId);

    @Query("select p.status from MeetPlan p where p.id = :planId")
    Status findStatusById(Long planId);

    Integer countByMeetIdAndStatus(Long meetId, Status status);

    boolean existsByIdAndStatus(Long id, Status status);

    Optional<MeetPlan> findByIdAndStatus(Long id, Status status);

    List<Long> findIdsByMeetIdAndCreatorIdAndStatus(Long meetId, Long creatorId, Status status);

    List<Long> findIdsByMeetIdAndStatus(Long meetId, Status status);
}
