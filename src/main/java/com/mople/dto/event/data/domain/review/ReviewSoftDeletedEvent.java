package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.REVIEW_SOFT_DELETED;

@JsonTypeName(REVIEW_SOFT_DELETED)
@Builder
@Getter
public class ReviewSoftDeletedEvent implements DomainEvent {

    private final Long meetId;
    private final Long planId;
    private final Long reviewId;
    private final Long reviewDeletedBy;
}
