package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.REVIEW_UPDATE;

@JsonTypeName(REVIEW_UPDATE)
@Builder
@Getter
public class ReviewUpdatedEvent implements DomainEvent {

    private final Long reviewId;
    private final Long reviewUpdatedBy;
    private final boolean isFirstUpload;
}
