package com.mople.global.event.handler.domain.impl.global;

import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSendHandler implements DomainEventHandler<NotifyRequestedEvent> {

    private final NotificationSendService sendService;

    @Override
    public Class<NotifyRequestedEvent> getHandledType() {
        return NotifyRequestedEvent.class;
    }

    @Override
    public void handle(NotifyRequestedEvent event) {
        sendService.sendMultiNotification(event);
    }
}
