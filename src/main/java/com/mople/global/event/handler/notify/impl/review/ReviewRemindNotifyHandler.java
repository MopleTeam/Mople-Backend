package com.mople.global.event.handler.notify.impl.review;

import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifyHandler implements NotifyEventHandler<ReviewRemindNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<ReviewRemindNotifyEvent> getHandledType() {
        return ReviewRemindNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewRemindNotifyEvent event) {
        return requestFactory.buildForTargets(event.targetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(ReviewRemindNotifyEvent event, List<Long> userIds) {
        return userIds.stream()
                .map(userId ->
                        Notification.builder()
                                .type(event.notifyType())
                                .meetId(event.meetId())
                                .reviewId(event.reviewId())
                                .payload(event.payload())
                                .userId(userId)
                                .build()
                )
                .toList();
    }
}
