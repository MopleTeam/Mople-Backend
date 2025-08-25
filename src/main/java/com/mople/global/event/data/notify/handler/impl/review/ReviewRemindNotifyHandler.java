package com.mople.global.event.data.notify.handler.impl.review;

import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
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
import static com.mople.global.enums.NotifyType.REVIEW_REMIND;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifyHandler implements NotifyHandler<ReviewRemindNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return REVIEW_REMIND;
    }

    @Override
    public Class<ReviewRemindNotifyEvent> getHandledType() {
        return ReviewRemindNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewRemindNotifyEvent data, NotificationEvent notify) {
        return requestFactory.getCreatorPushToken(data.getReviewCreatorId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(ReviewRemindNotifyEvent data, NotificationEvent notify, List<User> users) {
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
