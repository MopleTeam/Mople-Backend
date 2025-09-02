package com.mople.dto.event.data.domain.meet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.MEET_PURGE;

@JsonTypeName(MEET_PURGE)
@Builder
@Getter
public class MeetPurgeEvent implements DomainEvent {

    private final Long meetId;
}
