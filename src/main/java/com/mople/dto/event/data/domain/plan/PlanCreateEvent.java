package com.mople.dto.event.data.domain.plan;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.mople.global.enums.EventTypeNames.PLAN_CREATE;

@JsonTypeName(PLAN_CREATE)
@Builder
@Getter
public class PlanCreateEvent implements DomainEvent {

    private final Long meetId;
    private final String meetName;
    private final Long planId;
    private final String planName;
    private final LocalDateTime planTime;
    private final BigDecimal lat;
    private final BigDecimal lot;
    private final Long planCreatorId;
}
