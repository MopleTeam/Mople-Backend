package com.mople.dto.event.data.domain.review;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record ReviewImageRemoveEvent(
        Long reviewId,
        String imageUrl,
        Long imageDeletedBy
) implements DomainEvent {
}
