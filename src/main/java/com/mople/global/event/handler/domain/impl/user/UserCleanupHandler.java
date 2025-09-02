package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.notification.repository.FirebaseTokenRepository;
import com.mople.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCleanupHandler implements DomainEventHandler<UserDeletedEvent> {

    private final FirebaseTokenRepository tokenRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public Class<UserDeletedEvent> getHandledType() {
        return UserDeletedEvent.class;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        tokenRepository.deleteByUserId(event.getUserId());
        notificationRepository.deleteByUserId(event.getUserId());
    }
}
