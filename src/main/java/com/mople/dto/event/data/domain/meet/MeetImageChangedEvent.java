package com.mople.dto.event.data.domain.meet;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record MeetImageChangedEvent(
        Long meetId,
        String imageUrl,
        Long imageDeletedBy
) implements DomainEvent {
}
