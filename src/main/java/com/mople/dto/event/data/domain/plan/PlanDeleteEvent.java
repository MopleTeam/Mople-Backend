package com.mople.dto.event.data.domain.plan;

import lombok.Builder;

@Builder
public record PlanDeleteEvent(
        Long meetId,
        String meetName,
        Long planId,
        String planName,
        Long planDeletedBy
) {
}
