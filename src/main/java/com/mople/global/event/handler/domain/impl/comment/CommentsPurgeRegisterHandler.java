package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentsPurgeEvent;
import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.*;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_PURGE;

@Component
@RequiredArgsConstructor
public class CommentsPurgeRegisterHandler implements DomainEventHandler<CommentsSoftDeletedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<CommentsSoftDeletedEvent> getHandledType() {
        return CommentsSoftDeletedEvent.class;
    }

    @Override
    public void handle(CommentsSoftDeletedEvent event) {
        LocalDateTime runAt = LocalDateTime.now().plusDays(7);

        CommentsPurgeEvent purgeEvent = CommentsPurgeEvent.builder()
                .commentIds(event.getCommentIds())
                .build();

        outboxService.saveWithRunAt(COMMENTS_PURGE, POST, event.getPostId(), runAt, purgeEvent);
    }
}
