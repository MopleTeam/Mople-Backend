package com.mople.dto.event.data.domain.review;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record ReviewUploadEvent(
        Long reviewId,
        Long reviewUpdatedBy
) implements DomainEvent {
}
