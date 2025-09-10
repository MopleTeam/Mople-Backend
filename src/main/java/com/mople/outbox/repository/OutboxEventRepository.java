package com.mople.outbox.repository;

import com.mople.entity.event.OutboxEvent;
import com.mople.global.enums.event.AggregateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
              WITH picked AS (
                  SELECT outbox_id
                    FROM outbox_event
                   WHERE status = 'PENDING'
                     AND available_at <= now()
                   ORDER BY available_at, outbox_id
                   FOR UPDATE SKIP LOCKED
                   LIMIT :limit
              )
              UPDATE outbox_event o
                 SET available_at = now() + make_interval(secs => :leaseSec)
                FROM picked
               WHERE o.outbox_id = picked.outbox_id
            RETURNING o.*;
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit, int leaseSec);

    @Modifying(flushAutomatically = true)
    @Query(
            "update OutboxEvent o " +
            "   set o.status = com.mople.global.enums.event.OutboxStatus.CANCELED " +
            " where o.eventType = :eventType " +
            "   and o.aggregateType = :aggregateType " +
            "   and o.aggregateId = :aggregateId " +
            "   and o.status = com.mople.global.enums.event.OutboxStatus.PENDING"
    )
    int eventCanceled(String eventType, AggregateType aggregateType, Long aggregateId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update OutboxEvent o " +
            "   set o.status = com.mople.global.enums.event.OutboxStatus.PUBLISHED, " +
            "       o.publishedAt = CURRENT_TIMESTAMP " +
            " where o.eventId = :eventId " +
            "   and o.status = com.mople.global.enums.event.OutboxStatus.PENDING"
    )
    int eventPublished(String eventId);

    @Modifying(flushAutomatically = true)
    @Query(
            "update OutboxEvent o " +
            "   set o.status = com.mople.global.enums.event.OutboxStatus.SKIPPED, " +
            "       o.attempts = o.attempts + 1, " +
            "       o.lastError = :errorMessage " +
            " where o.eventId = :eventId " +
            "   and o.status = com.mople.global.enums.event.OutboxStatus.PENDING"
    )
    int eventSkip(String eventId, String errorMessage);

    @Modifying(flushAutomatically = true)
    @Query(value = """
               UPDATE outbox_event
                  SET attempts = attempts + 1,
                      last_error = :errorMessage,
                      status = CASE WHEN attempts + 1 >= :maxAttempts
                               THEN 'FAILED' ELSE 'PENDING' END,
                      available_at = CASE WHEN attempts + 1 >= :maxAttempts
                                     THEN available_at ELSE now() + make_interval(secs => :retrySec) END
                WHERE event_id = :eventId
                  AND status = 'PENDING'
            """, nativeQuery = true)
    int eventRetry(String eventId, String errorMessage, int retrySec, int maxAttempts);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE outbox_id IN (
               SELECT outbox_id FROM outbox_event
                WHERE status = 'PUBLISHED'
                  AND published_at < :before
                ORDER BY outbox_id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldPublished(LocalDateTime before, int limit);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE outbox_id IN (
               SELECT outbox_id FROM outbox_event
                WHERE status = 'CANCELED'
                  AND created_at < :before
                ORDER BY outbox_id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldCanceled(LocalDateTime before, int limit);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE outbox_id IN (
               SELECT outbox_id FROM outbox_event
                WHERE status = 'SKIPPED'
                  AND created_at < :before
                ORDER BY outbox_id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldSkipped(LocalDateTime before, int limit);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE outbox_id IN (
               SELECT outbox_id FROM outbox_event
                WHERE status = 'FAILED'
                  AND created_at < :before
                ORDER BY outbox_id
                LIMIT :limit
             )
            """, nativeQuery = true)
    int deleteOldFailed(LocalDateTime before, int limit);
}
