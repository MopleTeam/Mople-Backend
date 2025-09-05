package com.mople.dto.event.data.domain.meet;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record MeetPurgeEvent(
        Long meetId
) implements DomainEvent {
}
