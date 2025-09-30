package com.mople.dto.event.data.notify;

import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;

import java.util.Map;

public interface NotifyEvent {

    NotifyType notifyType();

    NotificationPayload payload();

    Map<String, String> routing();
}
