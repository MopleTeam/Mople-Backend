package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.EventTypeNames.PLAN_CREATE;

@JsonTypeName(PLAN_CREATE)
@Builder
@Getter
public class PlanCreateEvent implements DomainEvent {

    private final Long planId;
}
