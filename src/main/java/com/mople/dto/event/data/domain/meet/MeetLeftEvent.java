package com.mople.dto.event.data.domain.meet;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record MeetLeftEvent(
        Long meetId,
        Long leaveMemberId
) implements DomainEvent {
}
