package com.mople.entity.event;

import com.mople.global.enums.AggregateType;
import com.mople.global.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "outbox_id")
    private Long id;

    @Column(name = "event_id", length = 36, nullable = false, unique = true)
    private String eventId;

    @Column(name = "event_type", length = 100, nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregate_type", nullable = false)
    private AggregateType aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_version", nullable = false)
    private Integer eventVersion;

    @Lob
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime availableAt;

    @Column(name = "available_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder
    public OutboxEvent(
            String eventId,
            String eventType,
            AggregateType aggregateType,
            Long aggregateId,
            int eventVersion,
            LocalDateTime availableAt,
            String payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventVersion = eventVersion;
        this.availableAt = availableAt;
        this.payload = payload;
    }

    public void published() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void occurredError(Exception ex) {
        int nextAttempts = attempts + 1;
        long backoff = (long) Math.min(300, Math.pow(2, nextAttempts));

        this.attempts = nextAttempts;
        this.lastError = shorten(ex.getMessage());
        this.availableAt = LocalDateTime.now().plusSeconds(backoff);
        this.status = nextAttempts >= 10 ? OutboxStatus.FAILED : status;
    }

    private String shorten(String s) {
        if (s == null) return null;
        return s.length() > 800 ? s.substring(0, 800) : s;
    }
}
