package com.mople.notification.service;

import com.google.firebase.messaging.*;

import com.mople.core.exception.custom.BadRequestException;
import com.mople.dto.event.data.EventData;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.Action;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.global.event.data.notify.handler.NotifyHandlerRegistry;
import com.mople.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_NOTIFY_TYPE;

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
                                Long badgeCount = notificationRepository.countBadgeCount(
                                        user.getId(),
                                        Action.COMPLETE.name(),
                                        LocalDateTime.now().minusDays(30)
                                );

                                return buildMessage(notify, token, sendRequest, badgeCount);
                            })
                            .toList()
            );
        }

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

    private Message buildMessage(NotificationEvent notify, FirebaseToken token, NotifySendRequest sendRequest, Long badgeCount) {

        int nextBadgeCount = Math.toIntExact(badgeCount + 1);

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
                                                .setTitle(notify.payload().title())
                                                .setBody(notify.payload().message())
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
