package com.mople.global.event.data.notify.handler.impl.review;

import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.enums.NotifyType;
import com.mople.global.event.data.notify.NotificationEvent;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;
import static com.mople.global.enums.NotifyType.REVIEW_UPDATE;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyHandler implements NotifyHandler<ReviewUpdateNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return REVIEW_UPDATE;
    }

    @Override
    public Class<ReviewUpdateNotifyEvent> getHandledType() {
        return ReviewUpdateNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewUpdateNotifyEvent data, NotificationEvent notify) {
        return requestFactory.getReviewPushToken(data.getReviewUpdatedBy(), data.getReviewId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(ReviewUpdateNotifyEvent data, NotificationEvent notify, List<User> users) {
        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(getType())
                                .action(COMPLETE)
                                .meetId(data.getMeetId())
                                .reviewId(data.getReviewId())
                                .payload(notify.payload())
                                .user(u)
                                .build()
                )
                .toList();
    }
}
