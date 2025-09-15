package com.mople.dto.event.data.domain.plan;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.event.DeletionCause;
import lombok.Builder;

@Builder
public record PlanSoftDeletedEvent(
        Long planId,
        Long planDeletedBy,
        DeletionCause cause
) implements DomainEvent {
}
