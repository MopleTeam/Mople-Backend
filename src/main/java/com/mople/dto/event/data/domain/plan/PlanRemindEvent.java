package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.PLAN_REMIND;

@JsonTypeName(PLAN_REMIND)
@Builder
@Getter
public class PlanRemindEvent implements DomainEvent {

    private final Long planId;
}
