package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.user.UserImageChangedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.USER;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class UserImageChangedHandler implements DomainEventHandler<UserImageChangedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<UserImageChangedEvent> getHandledType() {
        return UserImageChangedEvent.class;
    }

    @Override
    public void handle(UserImageChangedEvent event) {
        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(USER)
                .aggregateId(event.userId())
                .imageUrl(event.imageUrl())
                .imageDeletedBy(event.imageDeletedBy())
                .build();

        outboxService.save(IMAGE_DELETED, USER, event.userId(), deletedEvent);
    }
}
