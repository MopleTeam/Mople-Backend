package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.EventTypeNames.PLAN_CREATED;

@JsonTypeName(PLAN_CREATED)
@Builder
@Getter
public class PlanCreatedEvent implements DomainEvent {

    private final Long planId;
    private final LocalDateTime planTime;
    private final Long planCreatorId;
}
