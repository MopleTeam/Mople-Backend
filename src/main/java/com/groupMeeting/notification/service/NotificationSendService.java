package com.groupMeeting.notification.service;

import com.google.firebase.messaging.*;

import com.groupMeeting.core.exception.custom.BadRequestException;
import com.groupMeeting.dto.event.data.EventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.notification.FirebaseToken;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandler;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandlerRegistry;
import com.groupMeeting.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_NOTIFY_TYPE;

@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final FirebaseMessaging sender;
    private final NotifyHandlerRegistry handlerRegistry;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendMultiNotification(NotificationEvent notify, NotifyType type, EventData data) {
        NotifyHandler<EventData> handler = findHandler(type, data);
        NotifySendRequest sendRequest = handler.getSendRequest(data, notify);

        if (!sendRequest.tokens().isEmpty()) {
            sender.sendEachAsync(
                    sendRequest
                            .tokens()
                            .stream()
                            .map(token -> {
                                User user = sendRequest.findUserByToken(token);
                                return buildMessage(notify, token, user, sendRequest);
                            })
                            .toList()
            );
        }
        sendRequest.users().forEach(User::updateBadgeCount);

        List<Notification> notifications = handler.getNotifications(data, notify, sendRequest.users());
        notificationRepository.saveAll(notifications);
    }

    @SuppressWarnings("unchecked")
    private NotifyHandler<EventData> findHandler(NotifyType type, EventData data) {
        NotifyHandler<? extends EventData> rawHandler = handlerRegistry.getHandler(type);
        Class<? extends EventData> handledType = handlerRegistry.getHandledType(type);

        if (!handledType.isInstance(data)) {
            throw new BadRequestException(NOT_FOUND_NOTIFY_TYPE);
        }

        return (NotifyHandler<EventData>) rawHandler;
    }

    private Message buildMessage(NotificationEvent notify, FirebaseToken token, User findUser, NotifySendRequest sendRequest) {
        return Message
                .builder()
                .setNotification(
                        com.google.firebase.messaging.Notification
                                .builder()
                                .setTitle(notify.payload().title())
                                .setBody(notify.payload().message())
                                .build()
                )
                .putAllData(notify.body())
                .setApnsConfig(
                        ApnsConfig
                                .builder()
                                .setAps(Aps.builder()
                                        .setSound("default")
                                        .setBadge(findUser.getBadgeCount() + 1)
                                        .build()
                                )
                                .build()
                )
                .setAndroidConfig(
                        AndroidConfig.builder()
                                .setTtl(3600)
                                .setNotification(
                                        AndroidNotification
                                                .builder()
                                                .setTitle(notify.payload().title())
                                                .setBody(notify.payload().message())
                                                .setDefaultSound(true)
                                                .setNotificationCount(findUser.getBadgeCount() + 1)
                                                .build()
                                )
                                .build()
                )
                .setToken(sendRequest.validToken(token))
                .build();
    }
}
