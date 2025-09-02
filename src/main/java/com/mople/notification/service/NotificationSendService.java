package com.mople.notification.service;

import com.google.firebase.messaging.*;

import com.mople.core.exception.custom.BadRequestException;
import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.global.event.handler.notify.NotifyHandlerRegistry;
import com.mople.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_NOTIFY_TYPE;

@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final FirebaseMessaging sender;
    private final NotifyHandlerRegistry handlerRegistry;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendMultiNotification(NotifyEvent event) {
        NotifyEventHandler<NotifyEvent> handler = findHandler(event);
        NotifySendRequest sendRequest = handler.getSendRequest(event);

        if (!sendRequest.tokens().isEmpty()) {
            sender.sendEachAsync(
                    sendRequest
                            .tokens()
                            .stream()
                            .map(token -> {
                                User user = sendRequest.findUserByToken(token);
                                Long badgeCount = notificationRepository.countBadgeCount(
                                        user.getId(),
                                        Action.COMPLETE.name()
                                );

                                return buildMessage(event, token, sendRequest, badgeCount);
                            })
                            .toList()
            );
        }

        List<Notification> notifications = handler.getNotifications(event, sendRequest.users());
        notificationRepository.saveAll(notifications);
    }

    @SuppressWarnings("unchecked")
    private NotifyEventHandler<NotifyEvent> findHandler(NotifyEvent event) {
        NotifyEventHandler<? extends NotifyEvent> rawHandler = handlerRegistry.getHandler(event.notifyType());
        Class<? extends NotifyEvent> handledType = handlerRegistry.getHandledType(event.notifyType());

        if (!handledType.isInstance(event)) {
            throw new BadRequestException(NOT_FOUND_NOTIFY_TYPE);
        }

        return (NotifyEventHandler<NotifyEvent>) rawHandler;
    }

    private Message buildMessage(NotifyEvent event, FirebaseToken token, NotifySendRequest sendRequest, Long badgeCount) {

        int nextBadgeCount = Math.toIntExact(badgeCount + 1);

        return Message
                .builder()
                .setNotification(
                        com.google.firebase.messaging.Notification
                                .builder()
                                .setTitle(event.payload().title())
                                .setBody(event.payload().message())
                                .build()
                )
                .putAllData(event.routing())
                .setApnsConfig(
                        ApnsConfig
                                .builder()
                                .setAps(Aps.builder()
                                        .setSound("default")
                                        .setBadge(nextBadgeCount)
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
                                                .setTitle(event.payload().title())
                                                .setBody(event.payload().message())
                                                .setDefaultSound(true)
                                                .setNotificationCount(nextBadgeCount)
                                                .build()
                                )
                                .build()
                )
                .setToken(sendRequest.validToken(token))
                .build();
    }
}
