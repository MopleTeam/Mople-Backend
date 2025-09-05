package com.mople.dto.event.data.domain.meet;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.MEET_IMAGE_CHANGED;

@JsonTypeName(MEET_IMAGE_CHANGED)
@Builder
@Getter
public class MeetImageChangedEvent implements DomainEvent {

    private final Long meetId;
    private final String imageUrl;
    private final Long imageDeletedBy;
}
