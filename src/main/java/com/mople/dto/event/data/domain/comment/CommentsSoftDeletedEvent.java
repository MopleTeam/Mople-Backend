package com.mople.dto.event.data.domain.comment;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.CommentTarget;
import lombok.Builder;

import java.util.List;

@Builder
public record CommentsSoftDeletedEvent(
        CommentTarget target,
        Long targetId,
        List<Long> commentIds,
        Long commentsDeletedBy
) implements DomainEvent {
}
