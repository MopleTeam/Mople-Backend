package com.mople.global.event.handler.notify;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;

import java.util.List;

public interface NotifyEventHandler<T extends NotifyEvent> {

    Class<T> getHandledType();

    NotifySendRequest getSendRequest(T event);

    List<Notification> getNotifications(T event, List<Long> userIds);
}
