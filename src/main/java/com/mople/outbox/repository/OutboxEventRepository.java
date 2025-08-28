package com.mople.outbox.repository;

import com.mople.entity.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Modifying(clearAutomatically = true)
    @Query("""
              UPDATE OutboxEvent e
                 SET e.availableAt = :runAt
               WHERE e.status = 'PENDING'
                 AND e.aggregateType = :aggregateType
                 AND e.eventType = :eventType
                 AND e.aggregateId = :aggregateId
            """)
    int updateEvent(String aggregateType, Long aggregateId, String eventType, LocalDateTime runAt);

    @Modifying(clearAutomatically = true)
    @Query("""
              DELETE FROM OutboxEvent e
               WHERE e.status = 'PENDING'
                 AND e.aggregateType = :aggregateType
                 AND e.aggregateId = :aggregateId
            """)
    int deleteEventByAggregateType(String aggregateType, Long aggregateId);


    @Modifying(clearAutomatically = true)
    @Query("""
              DELETE FROM OutboxEvent e
               WHERE e.status = 'PENDING'
                 AND e.aggregateType = :aggregateType
                 AND e.eventType = :eventType
                 AND e.aggregateId = :aggregateId
            """)
    int deleteEventByEventType(String aggregateType, Long aggregateId, String eventType);

    @Query(value = """
              SELECT * FROM outbox_event
               WHERE status = 'PENDING'
                 AND available_at <= now()
            ORDER BY id
          FOR UPDATE SKIP LOCKED
                     LIMIT :limit;
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE id IN (
               SELECT id FROM outbox_event
                WHERE status = 'PUBLISHED'
                  AND created_at < :before
                ORDER BY id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldPublished(LocalDateTime before, int limit);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE id IN (
               SELECT id FROM outbox_event
                WHERE status = 'FAILED'
                  AND created_at < :before
                ORDER BY id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldFailed(LocalDateTime before, int limit);

}
