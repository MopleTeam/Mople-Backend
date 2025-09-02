package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.EventTypeNames.PLAN_UPDATED;

@JsonTypeName(PLAN_UPDATED)
@Builder
@Getter
public class PlanUpdatedEvent implements DomainEvent {

    private final Long planId;
    private final Long planUpdatedBy;
    private final LocalDateTime newTime;
    private final LocalDateTime preTime;
}
