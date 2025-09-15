package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.repository.TopicRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.USER;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class UserCleanupHandler implements DomainEventHandler<UserDeletedEvent> {

    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final OutboxService outboxService;

    @Override
    public Class<UserDeletedEvent> getHandledType() {
        return UserDeletedEvent.class;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        notificationRepository.deleteByUserId(event.userId());
        topicRepository.deleteByUserId(event.userId());

        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(USER)
                .aggregateId(event.userId())
                .imageUrl(event.userProfileImg())
                .imageDeletedBy(event.userId())
                .build();

        outboxService.save(IMAGE_DELETED, USER, event.userId(), deletedEvent);
    }
}
