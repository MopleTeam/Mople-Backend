package com.mople.dto.event.data.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.event.data.domain.plan.PlanDeleteEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.domain.review.ReviewCreateEvent;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;

import static com.mople.global.enums.EventTypeNames.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlanCreateEvent.class, name = PLAN_CREATE),
        @JsonSubTypes.Type(value = PlanDeleteEvent.class, name = PLAN_DELETE),
        @JsonSubTypes.Type(value = PlanRemindEvent.class, name = PLAN_REMIND),
        @JsonSubTypes.Type(value = ReviewCreateEvent.class, name = REVIEW_CREATE),
        @JsonSubTypes.Type(value = ReviewRemindEvent.class, name = REVIEW_REMIND)
})
public interface DomainEvent {
}
