package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.EventTypeNames.PLAN_TIME_CHANGED;

@JsonTypeName(PLAN_TIME_CHANGED)
@Builder
@Getter
public class PlanTimeChangedEvent implements DomainEvent {

    private final Long planId;
    private final Long timeChangedBy;
    private final LocalDateTime oldTime;
    private final LocalDateTime newTime;
}
