package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.mople.global.enums.event.EventTypeNames.COMMENT_CREATED;

@JsonTypeName(COMMENT_CREATED)
@Builder
@Getter
public class CommentCreatedEvent implements DomainEvent {

    private final Long meetId;
    private final Long commentId;
    private final Long senderId;
    private final List<Long> mentions;
    private final Long parentId;
}
