package com.mople.dto.event.data.domain.meet;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record HostChangedEvent(
        Long meetId,
        Long oldHostId,
        Long newHostId
) implements DomainEvent {
}
