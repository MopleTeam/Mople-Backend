package com.groupMeeting.global.event.data.notify.handler;

import com.groupMeeting.dto.event.data.EventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;

import java.util.List;

public interface NotifyHandler<T extends EventData> {

    NotifyType getType();

    Class<T> getHandledType();

    NotifySendRequest getSendRequest(T data, NotificationEvent notify);

    List<Notification> getNotifications(T data, NotificationEvent notify, List<User> users);
}
