package com.mople.dto.event.data.domain.notify;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;

import java.util.List;
import java.util.Map;

public record NotifyRequestedEvent(
        NotifyType notifyType,
        NotificationPayload payload,
        Map<String, String> routing,
        List<Long> notificationIds
) implements DomainEvent {
    public NotifyRequestedEvent(NotifyEvent event, List<Long> notificationIds) {
        this(
                event.notifyType(),
                event.payload(),
                event.routing(),
                notificationIds
        );
    }
}
