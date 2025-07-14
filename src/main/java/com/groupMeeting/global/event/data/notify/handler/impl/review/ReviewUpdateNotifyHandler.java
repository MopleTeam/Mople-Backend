package com.groupMeeting.global.event.data.notify.handler.impl.review;

import com.groupMeeting.dto.event.data.review.ReviewUpdateEventData;
import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.NotifyType;
import com.groupMeeting.global.event.data.notify.NotificationEvent;
import com.groupMeeting.global.event.data.notify.handler.NotifyHandler;
import com.groupMeeting.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.groupMeeting.global.enums.Action.COMPLETE;
import static com.groupMeeting.global.enums.NotifyType.REVIEW_UPDATE;

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
