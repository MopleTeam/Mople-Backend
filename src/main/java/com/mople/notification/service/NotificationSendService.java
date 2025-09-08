package com.mople.notification.service;

import com.google.firebase.messaging.*;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.global.event.handler.notify.NotifyHandlerRegistry;
import com.mople.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final FirebaseMessaging sender;
    private final NotifyHandlerRegistry registry;
    private final NotificationRepository notificationRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void sendMultiNotification(NotifyEvent event) {
        @SuppressWarnings("unchecked")
        NotifyEventHandler<NotifyEvent> handler = (NotifyEventHandler<NotifyEvent>) registry.getSingleHandler(event);
        NotifySendRequest sendRequest = handler.getSendRequest(event);

        List<Notification> notifications = handler.getNotifications(event, sendRequest.userIds());
        notificationRepository.saveAll(notifications);

        if (sendRequest.tokens().isEmpty()) {
            return;
        }

        List<Message> messages = sendRequest
                .tokens()
                .stream()
                .map(token -> {
                    Long userId = sendRequest.findUserByToken(token);
                    Long badgeCount = notificationRepository.countBadgeCount(userId);

                    return buildMessage(event, token, sendRequest, badgeCount);
                })
                .toList();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sender.sendEachAsync(messages);
            }
        });
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
