package com.mople.dto.event.data.domain.comment;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

import java.util.List;

@Builder
public record CommentsSoftDeletedEvent(
        Long postId,
        List<Long> commentIds,
        Long commentsDeletedBy
) implements DomainEvent {
}
