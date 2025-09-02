package com.mople.dto.event.data.domain.comment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.mople.global.enums.event.EventTypeNames.COMMENTS_PURGE;

@JsonTypeName(COMMENTS_PURGE)
@Builder
@Getter
public class CommentPurgeEvent implements DomainEvent {

    private final List<Long> commentIds;
}
