package com.mople.dto.event.data.domain.comment;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

import java.util.List;

@Builder
public record CommentMentionAddedEvent(
        Long postId,
        Long commentId,
        Long commentWriterId,
        List<Long> originMentions,
        Long parentId
) implements DomainEvent {
}
