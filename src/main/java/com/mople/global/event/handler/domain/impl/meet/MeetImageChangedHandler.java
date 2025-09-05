package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.meet.MeetImageChangedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class MeetImageChangedHandler implements DomainEventHandler<MeetImageChangedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<MeetImageChangedEvent> getHandledType() {
        return MeetImageChangedEvent.class;
    }

    @Override
    public void handle(MeetImageChangedEvent event) {
        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(MEET)
                .aggregateId(event.meetId())
                .imageUrl(event.imageUrl())
                .imageDeletedBy(event.imageDeletedBy())
                .build();

        outboxService.save(IMAGE_DELETED, MEET, event.meetId(), deletedEvent);
    }
}
