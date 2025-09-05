package com.mople.dto.event.data.domain.plan;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record PlanRemindEvent(
        Long planId
) implements DomainEvent {
}
