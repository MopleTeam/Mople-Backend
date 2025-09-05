package com.mople.dto.event.data.domain.user;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record UserImageChangedEvent(
        Long userId,
        String imageUrl,
        Long imageDeletedBy
) implements DomainEvent {
}
