package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.meet.MeetPurgeEvent;
import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.MEET_PURGE;

@Component
@RequiredArgsConstructor
public class MeetPurgeRegisterHandler implements DomainEventHandler<MeetSoftDeletedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<MeetSoftDeletedEvent> getHandledType() {
        return MeetSoftDeletedEvent.class;
    }

    @Override
    public void handle(MeetSoftDeletedEvent event) {
        LocalDateTime runAt = LocalDateTime.now().plusDays(7);

        MeetPurgeEvent purgeEvent = MeetPurgeEvent.builder()
                .meetId(event.getMeetId())
                .build();

        outboxService.saveWithRunAt(MEET_PURGE, MEET, event.getMeetId(), runAt, purgeEvent);
    }
}
