package com.mople.dto.event.data.domain.global;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record WeatherRefreshRequestedEvent(
        Long planId
) implements DomainEvent {
}
