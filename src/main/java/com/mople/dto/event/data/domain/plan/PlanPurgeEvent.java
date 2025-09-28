package com.mople.dto.event.data.domain.plan;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record PlanPurgeEvent(
        Long planId
) implements DomainEvent {
}
