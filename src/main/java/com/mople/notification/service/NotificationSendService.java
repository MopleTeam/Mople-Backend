package com.mople.notification.service;

import com.google.firebase.messaging.*;

import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.FirebaseToken;
import com.mople.entity.notification.Notification;
import com.mople.notification.reader.NotificationTokenReader;
import com.mople.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.mople.dto.response.notification.NotifySendRequest.ofRequest;

@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final FirebaseMessaging sender;
    private final NotificationRepository notificationRepository;
    private final NotificationTokenReader tokenReader;

    @Transactional(propagation = Propagation.MANDATORY)
    public void sendMultiNotification(NotifyRequestedEvent event) {
        List<Notification> notifications = notificationRepository.findAll(event.notificationIds());
        notifications.forEach(Notification::publishNotification);

        List<Long> userIds = notifications.stream().map(Notification::getUserId).toList();
        List<FirebaseToken> tokens = tokenReader.findTokensWithPushTopic(userIds, event.notifyType().getTopic());

        if (tokens.isEmpty()) {
            return;
        }

        Map<Long, Long> badgeMap = tokenReader.getBadgeMap(userIds);
        List<NotifySendRequest> sendRequests = ofRequest(tokens, badgeMap);

        List<Message> messages = sendRequests
                .stream()
                .map(request -> buildMessage(event, request))
                .toList();

        sender.sendEachAsync(messages);
    }

    private Message buildMessage(NotifyRequestedEvent event, NotifySendRequest request) {
        int nextBadgeCount = Math.toIntExact(request.badgeCount() + 1);

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
                .setToken(request.token())
                .build();
    }
}
