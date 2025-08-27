package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.EventTypeNames.PLAN_DELETE;

@JsonTypeName(PLAN_DELETE)
@Builder
@Getter
public class PlanDeleteEvent implements DomainEvent {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final Long planDeletedBy;
}
