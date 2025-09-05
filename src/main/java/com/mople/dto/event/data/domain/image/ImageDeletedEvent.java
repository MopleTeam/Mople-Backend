package com.mople.dto.event.data.domain.image;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.event.AggregateType;
import lombok.Builder;

@Builder
public record ImageDeletedEvent(
        AggregateType aggregateType,
        Long aggregateId,
        String imageUrl,
        Long imageDeletedBy
) implements DomainEvent {
}
