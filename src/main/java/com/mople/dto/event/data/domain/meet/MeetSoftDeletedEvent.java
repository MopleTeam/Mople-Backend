package com.mople.dto.event.data.domain.meet;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record MeetSoftDeletedEvent(
        Long meetId,
        Long meetDeletedBy
) implements DomainEvent {

}
