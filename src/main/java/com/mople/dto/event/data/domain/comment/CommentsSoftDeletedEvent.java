package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;

@JsonTypeName(COMMENTS_SOFT_DELETED)
@Builder
@Getter
public class CommentsSoftDeletedEvent implements DomainEvent {

    private final Long postId;
    private final List<Long> commentIds;
    private final Long commentsDeletedBy;
}
