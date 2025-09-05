package com.mople.dto.event.data.domain.review;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record ReviewSoftDeletedEvent(
        Long planId,
        Long reviewId,
        Long reviewDeletedBy
) implements DomainEvent {
}
