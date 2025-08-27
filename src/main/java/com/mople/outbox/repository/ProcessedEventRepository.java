package com.mople.outbox.repository;

import com.mople.entity.event.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

    @Modifying
    @Query(value = """
      DELETE FROM processed_event
       WHERE processed_at < :before
       LIMIT :limit
      """, nativeQuery = true)
    int deleteOldProcessed(LocalDateTime before, int limit);
}