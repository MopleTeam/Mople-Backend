package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.mople.global.enums.event.EventTypeNames.COMMENT_MENTION_ADDED;

@JsonTypeName(COMMENT_MENTION_ADDED)
@Builder
@Getter
public class CommentMentionAddedEvent implements DomainEvent {

    private final Long postId;
    private final Long commentId;
    private final Long commentWriterId;
    private final List<Long> originMentions;
    private final Long parentId;
}
