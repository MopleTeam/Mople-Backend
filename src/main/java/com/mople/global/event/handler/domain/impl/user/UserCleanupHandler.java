package com.mople.global.event.handler.domain.impl.user;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.repository.TopicRepository;
import com.mople.outbox.service.OutboxService;
import com.mople.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.USER;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class UserCleanupHandler implements DomainEventHandler<UserDeletedEvent> {

    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Override
    public Class<UserDeletedEvent> getHandledType() {
        return UserDeletedEvent.class;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        notificationRepository.deleteByUserId(event.userId());
        topicRepository.deleteByUserId(event.userId());

        User user = userRepository.findByIdAndStatus(event.userId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_USER));

        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(USER)
                .aggregateId(event.userId())
                .imageUrl(user.getProfileImg())
                .imageDeletedBy(event.userId())
                .build();

        outboxService.save(IMAGE_DELETED, USER, event.userId(), deletedEvent);
    }
}
