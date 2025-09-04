package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.COMMENT_CREATED;

@JsonTypeName(COMMENT_CREATED)
@Builder
@Getter
public class CommentCreatedEvent implements DomainEvent {

    private final Long postId;
    private final Long commentId;
    private final Long commentWriterId;
    private final Boolean isExistMention;
    private final Long parentId;
}
