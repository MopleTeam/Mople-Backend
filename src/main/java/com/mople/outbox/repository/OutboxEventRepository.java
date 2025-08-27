package com.mople.outbox.repository;

import com.mople.entity.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
      SELECT * FROM outbox_event
       WHERE status = 'PENDING'
         AND available_at <= now()
       ORDER BY id
       FOR UPDATE SKIP LOCKED
       LIMIT :limit
      """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit);

    @Modifying
    @Query(value = """
        UPDATE outbox_event
           SET available_at = :runAt
         WHERE status = 'PENDING'
           AND aggregate_type = 'PLAN'
           AND event_type = 'PLAN_REMIND'
           AND aggregate_id = :planId
        """, nativeQuery = true)
    int reschedulePlanRemind(Long planId, LocalDateTime runAt);

    @Modifying
    @Query(value = """
        DELETE FROM outbox_event
         WHERE status = 'PENDING'
           AND aggregate_type = 'PLAN'
           AND event_type = 'PLAN_REMIND'
           AND aggregate_id = :planId
        """, nativeQuery = true)
    int deletePendingPlanRemind(Long planId);
}
