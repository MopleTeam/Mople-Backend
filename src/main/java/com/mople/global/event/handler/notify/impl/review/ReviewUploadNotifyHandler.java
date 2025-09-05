package com.mople.global.event.handler.notify.impl.review;

import com.mople.dto.event.data.notify.review.ReviewUploadNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.global.event.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewUploadNotifyHandler implements NotifyEventHandler<ReviewUploadNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<ReviewUploadNotifyEvent> getHandledType() {
        return ReviewUploadNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewUploadNotifyEvent event) {
        return requestFactory.buildForTargets(event.getTargetIds(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(ReviewUploadNotifyEvent event, List<Long> userIds) {
        return userIds.stream()
                .map(userId ->
                        Notification.builder()
                                .type(event.notifyType())
                                .meetId(event.getMeetId())
                                .reviewId(event.getReviewId())
                                .payload(event.payload())
                                .userId(userId)
                                .build()
                )
                .toList();
    }
}
