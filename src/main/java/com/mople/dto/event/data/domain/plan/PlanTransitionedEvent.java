package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.PLAN_TRANSITIONED;

@JsonTypeName(PLAN_TRANSITIONED)
@Builder
@Getter
public class PlanTransitionedEvent implements DomainEvent {

    private final Long planId;
    private final Long reviewId;
}
