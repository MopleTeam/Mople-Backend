package com.mople.dto.event.data.notify;

import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;

import java.util.List;
import java.util.Map;

public interface NotifyEvent {

    NotificationPayload payload();

    Map<String, String> routing();

    List<Long> targetIds();

    NotifyType notifyType();
}
