package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.REVIEW_CREATED;

@JsonTypeName(REVIEW_CREATED)
@Builder
@Getter
public class ReviewCreatedEvent implements DomainEvent {

    private final Long reviewId;
}
