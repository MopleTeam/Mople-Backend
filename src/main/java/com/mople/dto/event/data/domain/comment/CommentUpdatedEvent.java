package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.mople.global.enums.event.EventTypeNames.COMMENT_UPDATED;

@JsonTypeName(COMMENT_UPDATED)
@Builder
@Getter
public class CommentUpdatedEvent implements DomainEvent {

    private final Long meetId;
    private final Long commentId;
    private final Long senderId;
    private final List<Long> originMentions;
    private final List<Long> newMentions;
    private final Long parentId;
}
