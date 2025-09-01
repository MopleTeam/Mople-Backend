package com.mople.dto.event.data.domain.review;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.REVIEW_REMIND;

@JsonTypeName(REVIEW_REMIND)
@Builder
@Getter
public class ReviewRemindEvent implements DomainEvent {

    private final Long reviewId;
}
