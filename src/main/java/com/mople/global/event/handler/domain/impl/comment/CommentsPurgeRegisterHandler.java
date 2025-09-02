package com.mople.global.event.handler.domain.impl.comment;

import com.mople.dto.event.data.domain.comment.CommentPurgeEvent;
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

        CommentPurgeEvent purgeEvent = CommentPurgeEvent.builder()
                .commentIds(event.getCommentIds())
                .build();

        if (event.getReviewId() == null) {
            outboxService.saveWithRunAt(COMMENTS_PURGE, PLAN, event.getPlanId(), runAt, purgeEvent);
        } else if (event.getPlanId() == null) {
            outboxService.saveWithRunAt(COMMENTS_PURGE, REVIEW, event.getReviewId(), runAt, purgeEvent);
        }

    }
}
