package com.mople.dto.event.data.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mople.dto.event.data.domain.comment.CommentCreatedEvent;
import com.mople.dto.event.data.domain.comment.CommentMentionAddedEvent;
import com.mople.dto.event.data.domain.comment.CommentsPurgeEvent;
import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.meet.*;
import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.plan.*;
import com.mople.dto.event.data.domain.review.*;
import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.dto.event.data.domain.user.UserImageChangedEvent;

import static com.mople.global.enums.event.EventTypeNames.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        // User
        @JsonSubTypes.Type(value = UserDeletedEvent.class, name = USER_DELETED),
        @JsonSubTypes.Type(value = UserImageChangedEvent.class, name = USER_IMAGE_CHANGED),

        // Meet
        @JsonSubTypes.Type(value = MeetSoftDeletedEvent.class, name = MEET_SOFT_DELETED),
        @JsonSubTypes.Type(value = MeetPurgeEvent.class, name = MEET_PURGE),
        @JsonSubTypes.Type(value = MeetLeftEvent.class, name = MEET_LEFT),
        @JsonSubTypes.Type(value = MeetJoinedEvent.class, name = MEET_JOINED),
        @JsonSubTypes.Type(value = MeetImageChangedEvent.class, name = MEET_IMAGE_CHANGED),

        // Plan
        @JsonSubTypes.Type(value = PlanSoftDeletedEvent.class, name = PLAN_SOFT_DELETED),
        @JsonSubTypes.Type(value = PlanPurgeEvent.class, name = PLAN_PURGE),
        @JsonSubTypes.Type(value = PlanCreatedEvent.class, name = PLAN_CREATED),
        @JsonSubTypes.Type(value = PlanRemindEvent.class, name = PLAN_REMIND),
        @JsonSubTypes.Type(value = PlanTimeChangedEvent.class, name = PLAN_TIME_CHANGED),
        @JsonSubTypes.Type(value = PlanTransitionedEvent.class, name = PLAN_TRANSITIONED),

        // Review
        @JsonSubTypes.Type(value = ReviewSoftDeletedEvent.class, name = REVIEW_SOFT_DELETED),
        @JsonSubTypes.Type(value = ReviewPurgeEvent.class, name = REVIEW_PURGE),
        @JsonSubTypes.Type(value = ReviewRemindEvent.class, name = REVIEW_REMIND),
        @JsonSubTypes.Type(value = ReviewUploadEvent.class, name = REVIEW_UPLOAD),
        @JsonSubTypes.Type(value = ReviewImageRemoveEvent.class, name = REVIEW_IMAGE_REMOVE),

        // Comment
        @JsonSubTypes.Type(value = CommentsSoftDeletedEvent.class, name = COMMENTS_SOFT_DELETED),
        @JsonSubTypes.Type(value = CommentsPurgeEvent.class, name = COMMENTS_PURGE),
        @JsonSubTypes.Type(value = CommentCreatedEvent.class, name = COMMENT_CREATED),
        @JsonSubTypes.Type(value = CommentMentionAddedEvent.class, name = COMMENT_MENTION_ADDED),

        // Image
        @JsonSubTypes.Type(value = ImageDeletedEvent.class, name = IMAGE_DELETED),

        // Notify
        @JsonSubTypes.Type(value = NotifyRequestedEvent.class, name = NOTIFY_REQUESTED)
})
public interface DomainEvent {
}
