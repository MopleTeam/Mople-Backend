package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.PLAN_PURGE;

@JsonTypeName(PLAN_PURGE)
@Builder
@Getter
public class PlanPurgeEvent implements DomainEvent {

    private final Long planId;
}
