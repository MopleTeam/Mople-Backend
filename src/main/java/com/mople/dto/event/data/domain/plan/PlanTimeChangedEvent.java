package com.mople.dto.event.data.domain.plan;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlanTimeChangedEvent(
        Long planId,
        Long timeChangedBy,
        LocalDateTime oldTime,
        LocalDateTime newTime
) implements DomainEvent {
}
