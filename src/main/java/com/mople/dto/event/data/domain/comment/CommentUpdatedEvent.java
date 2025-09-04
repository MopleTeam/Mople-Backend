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

    private final Long postId;
    private final Long commentId;
    private final Long commentWriterId;
    private final Boolean isExistMention;
    private final List<Long> originMentionedIds;
    private final Long parentId;
}
