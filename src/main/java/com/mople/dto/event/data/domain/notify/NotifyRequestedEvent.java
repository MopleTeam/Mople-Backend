package com.mople.dto.event.data.domain.notify;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.dto.response.notification.NotificationSnapshot;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record NotifyRequestedEvent(
        NotifyType notifyType,
        NotificationSnapshot snapshot,
        List<Long> targetIds,
        Map<String, String> routing
) implements DomainEvent {
}
