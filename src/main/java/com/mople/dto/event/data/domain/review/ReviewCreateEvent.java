package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.EventTypeNames.REVIEW_CREATE;

@JsonTypeName(REVIEW_CREATE)
@Builder
@Getter
public class ReviewCreateEvent implements DomainEvent {

    private final Long meetId;
    private final String meetName;
    private final Long reviewId;
    private final String reviewName;
    private final Long reviewCreatorId;
    private final boolean isUpload;
}
