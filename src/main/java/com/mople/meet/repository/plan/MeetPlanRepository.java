package com.mople.meet.repository.plan;

import com.mople.entity.meet.plan.MeetPlan;

import com.mople.global.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetPlanRepository extends JpaRepository<MeetPlan, Long>{

    @Query("select p from MeetPlan p where p.planTime < :time")
    List<MeetPlan> findPreviousPlanAll(LocalDateTime time);

    @Query("select p from MeetPlan p where p.id in :planIds")
    List<MeetPlan> findPlanAndTime(List<Long> planIds);

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

    @Query("select p.id from MeetPlan p where p.meetId = :meetId and p.creatorId = :creatorId")
    List<Long> findIdsByMeetIdAndCreatorId(Long meetId, Long creatorId);

    @Modifying(clearAutomatically = true)
    @Query("update MeetPlan p set p.status = :status, p.deletedAt = now(), p.deletedBy = :userId where p.id = :planId and p.status <> :status")
    int softDelete(Status status, Long planId, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update MeetPlan p set p.status = :status, p.deletedAt = now(), p.deletedBy = :userId where p.id in :planIds and p.status <> :status")
    int softDeleteAll(Status status, List<Long> planIds, Long userId);

    @Query("select p.id from MeetPlan p where p.meetId = :meetId")
    List<Long> findIdsByMeetId(Long meetId);

    @Query("select p.status from MeetPlan p where p.id = :planId")
    Status findStatusById(Long planId);

    Integer countByMeetId(Long meetId);
}
