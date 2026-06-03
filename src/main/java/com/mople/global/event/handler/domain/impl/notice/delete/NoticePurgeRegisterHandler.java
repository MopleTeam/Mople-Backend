package com.mople.global.event.handler.domain.impl.notice.delete;

import com.mople.dto.event.data.domain.notice.NoticePurgeEvent;
import com.mople.dto.event.data.domain.notice.NoticeSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.NOTICE;
import static com.mople.global.enums.event.EventTypeNames.NOTICE_PURGE;

@Component
@RequiredArgsConstructor
public class NoticePurgeRegisterHandler implements DomainEventHandler<NoticeSoftDeletedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<NoticeSoftDeletedEvent> getHandledType() {
        return NoticeSoftDeletedEvent.class;
    }

    @Override
    public void handle(NoticeSoftDeletedEvent event) {
        LocalDateTime runAt = LocalDateTime.now().plusDays(7);

        NoticePurgeEvent purgeEvent = NoticePurgeEvent.builder()
                .noticeId(event.noticeId())
                .build();

        outboxService.saveWithRunAt(NOTICE_PURGE, NOTICE, event.noticeId(), runAt, purgeEvent);
    }
}
