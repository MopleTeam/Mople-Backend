package com.mople.global.event.data.notify.handler;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;

import java.util.List;

public interface NotifyHandler<T extends NotifyEvent> {

    Class<T> getHandledType();

    NotifySendRequest getSendRequest(T event);

    List<Notification> getNotifications(T event, List<User> users);
}
