package com.mople.global.event.data.notify.handler;

import com.mople.dto.event.data.EventData;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;

import java.util.List;

public interface NotifyHandler<T extends EventData> {

    NotifyType getType();

    Class<T> getHandledType();

    NotifySendRequest getSendRequest(T data, NotificationEvent notify);

    List<Notification> getNotifications(T data, NotificationEvent notify, List<User> users);
}
