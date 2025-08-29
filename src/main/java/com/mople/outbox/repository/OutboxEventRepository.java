package com.mople.outbox.repository;

import com.mople.entity.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
              WITH picked AS (
                  SELECT id
                    FROM outbox_event
                   WHERE status = 'PENDING'
                     AND available_at <= now()
                   ORDER BY available_at, id
                   FOR UPDATE SKIP LOCKED
                   LIMIT :limit
              )
              UPDATE outbox_event o
                 SET available_at = now() + make_interval(secs => :leaseSec)
                FROM picked
               WHERE o.id = picked.id
            RETURNING o.*;
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(int limit, int leaseSec);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE outbox_event
               SET status = 'PUBLISHED',
                   published_at = now()
             WHERE event_id = :eventId
            """, nativeQuery = true)
    int eventPublished(String eventId);

    @Modifying(clearAutomatically = true)
    @Query(value = """
               UPDATE outbox_event
                  SET attempts = attempts + 1,
                      last_error = :errorMessage,
                      status = CASE WHEN attempts + 1 >= :maxAttempts
                               THEN 'FAILED' ELSE 'PENDING' END,
                      available_at = CASE WHEN attempts + 1 >= :maxAttempts
                                     THEN available_at ELSE now() + make_interval(secs => :retrySec) END
                WHERE event_id = :eventId
            """, nativeQuery = true)
    int eventRetry(String eventId, String errorMessage, int retrySec, int maxAttempts);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            UPDATE outbox_event
               SET status = 'FAILED',
                   attempts = attempts + 1,
                   last_error = :errorMessage
             WHERE event_id = :eventId
            """, nativeQuery = true)
    int eventFailed(String eventId, String errorMessage);

    @Modifying(clearAutomatically = true)
    @Query(value = """
            DELETE FROM outbox_event
             WHERE id IN (
               SELECT id FROM outbox_event
                WHERE status = 'PUBLISHED'
                  AND published_at < :before
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
