package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.REVIEW_IMAGE_REMOVE;

@JsonTypeName(REVIEW_IMAGE_REMOVE)
@Builder
@Getter
public class ReviewImageRemoveEvent implements DomainEvent {

    private final Long reviewId;
    private final String imageUrl;
    private final Long imageDeletedBy;
}
