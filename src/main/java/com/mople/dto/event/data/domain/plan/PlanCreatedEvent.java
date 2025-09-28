package com.mople.dto.event.data.domain.plan;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PlanCreatedEvent(
        Long meetId,
        Long planId,
        LocalDateTime planTime,
        Long planCreatorId
) implements DomainEvent {
}
