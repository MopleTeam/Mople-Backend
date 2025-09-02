package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.event.DeletionCause;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.PLAN_SOFT_DELETED;

@JsonTypeName(PLAN_SOFT_DELETED)
@Builder
@Getter
public class PlanSoftDeletedEvent implements DomainEvent {

    private final Long planId;
    private final Long planDeletedBy;
    private final DeletionCause cause;
}
