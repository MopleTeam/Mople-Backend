package com.mople.global.event.data.notify.handler.impl.review;

import com.mople.dto.event.data.review.ReviewUpdateEventData;
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
public class ReviewUpdateNotifyHandler implements NotifyHandler<ReviewUpdateEventData> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return REVIEW_UPDATE;
    }

    @Override
    public Class<ReviewUpdateEventData> getHandledType() {
        return ReviewUpdateEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewUpdateEventData data, NotificationEvent notify) {
        return requestFactory.getReviewPushToken(data.getCreatorId(), data.getReviewId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(ReviewUpdateEventData data, NotificationEvent notify, List<User> users) {
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
