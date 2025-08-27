package com.mople.outbox.repository;

import com.mople.entity.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
