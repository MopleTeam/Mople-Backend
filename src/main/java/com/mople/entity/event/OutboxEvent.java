package com.mople.entity.event;

import com.mople.global.enums.event.AggregateType;
import com.mople.global.enums.event.OutboxStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Builder
    public OutboxEvent(
            String eventId,
            String eventType,
            AggregateType aggregateType,
            Long aggregateId,
            LocalDateTime availableAt,
            String payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.availableAt = availableAt;
        this.payload = payload;
    }
}
