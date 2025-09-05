package com.mople.dto.event.data.domain.comment;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record CommentCreatedEvent(
        Long postId,
        Long commentId,
        Long commentWriterId,
        Boolean isExistMention,
        Long parentId
) implements DomainEvent {
}
