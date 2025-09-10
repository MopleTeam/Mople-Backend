package com.mople.notification.service;

import com.google.firebase.messaging.*;

import com.mople.core.exception.custom.RetryableOutboxException;
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
import static com.mople.global.enums.ExceptionReturnCode.RETRIABLE;

@Service
@RequiredArgsConstructor
public class NotificationSendService {

    private final FirebaseMessaging sender;
    private final NotificationRepository notificationRepository;
    private final NotificationTokenReader tokenReader;

    @Transactional(
            propagation = Propagation.MANDATORY,
            noRollbackFor = RetryableOutboxException.class
    )
    public void sendMultiNotification(NotifyRequestedEvent event) {
        try {
            List<Notification> notifications = event.targetIds()
                    .stream()
                    .map(userId ->
                            Notification.builder()
                                    .type(event.notifyType())
                                    .meetId(event.snapshot().meetId())
                                    .planId(event.snapshot().planId())
                                    .reviewId(event.snapshot().reviewId())
                                    .payload(event.snapshot().payload())
                                    .userId(userId)
                                    .build()
                    )
                    .toList();

            notificationRepository.saveAll(notifications);

            List<FirebaseToken> tokens = tokenReader.findTokensWithPushTopic(event.targetIds(), event.notifyType().getTopic());

            if (tokens.isEmpty()) {
                return;
            }

            Map<Long, Long> badgeMap = tokenReader.getBadgeMap(event.targetIds());
            List<NotifySendRequest> sendRequests = ofRequest(tokens, badgeMap);

            List<Message> messages = sendRequests
                    .stream()
                    .map(request -> buildMessage(event, request))
                    .toList();

            sender.sendEachAsync(messages);
        } catch (Exception e) {
            throw new RetryableOutboxException(RETRIABLE);
        }
    }

    private Message buildMessage(NotifyRequestedEvent event, NotifySendRequest request) {
        int nextBadgeCount = Math.toIntExact(request.badgeCount() + 1);

        return Message
                .builder()
                .setNotification(
                        com.google.firebase.messaging.Notification
                                .builder()
                                .setTitle(event.snapshot().payload().title())
                                .setBody(event.snapshot().payload().message())
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
                                                .setTitle(event.snapshot().payload().title())
                                                .setBody(event.snapshot().payload().message())
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
