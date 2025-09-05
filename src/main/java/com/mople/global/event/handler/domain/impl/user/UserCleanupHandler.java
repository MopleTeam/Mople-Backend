package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCleanupHandler implements DomainEventHandler<UserDeletedEvent> {

    private final NotificationRepository notificationRepository;
    private final TopicRepository topicRepository;

    @Override
    public Class<UserDeletedEvent> getHandledType() {
        return UserDeletedEvent.class;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        notificationRepository.deleteByUserId(event.userId());
        topicRepository.deleteByUserId(event.userId());
    }
}
