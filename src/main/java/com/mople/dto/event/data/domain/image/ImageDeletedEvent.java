package com.mople.dto.event.data.domain.image;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.event.AggregateType;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@JsonTypeName(IMAGE_DELETED)
@Builder
@Getter
public class ImageDeletedEvent implements DomainEvent {

    private final AggregateType aggregateType;
    private final Long aggregateId;
    private final String imageUrl;
    private final Long imageDeletedBy;
}
