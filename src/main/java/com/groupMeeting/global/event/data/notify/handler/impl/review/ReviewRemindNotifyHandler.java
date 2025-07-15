package com.groupMeeting.global.event.data.notify.handler.impl.review;

import com.groupMeeting.dto.event.data.review.ReviewRemindEventData;
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
import static com.groupMeeting.global.enums.NotifyType.REVIEW_REMIND;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifyHandler implements NotifyHandler<ReviewRemindEventData> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public NotifyType getType() {
        return REVIEW_REMIND;
    }

    @Override
    public Class<ReviewRemindEventData> getHandledType() {
        return ReviewRemindEventData.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewRemindEventData data, NotificationEvent notify) {
        return requestFactory.getReviewCreatorPushToken(data.getCreatorId(), notify.topic());
    }

    @Override
    public List<Notification> getNotifications(ReviewRemindEventData data, NotificationEvent notify, List<User> users) {
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
