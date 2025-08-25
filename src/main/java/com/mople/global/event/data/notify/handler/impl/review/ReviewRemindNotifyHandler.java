package com.mople.global.event.data.notify.handler.impl.review;

import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.notify.handler.NotifyHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifyHandler implements NotifyHandler<ReviewRemindNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<ReviewRemindNotifyEvent> getHandledType() {
        return ReviewRemindNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewRemindNotifyEvent event) {
        return requestFactory.getCreatorPushToken(event.getReviewCreatorId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(ReviewRemindNotifyEvent event, List<User> users) {
        return users.stream()
                .map(u ->
                        Notification.builder()
                                .type(event.notifyType())
                                .action(COMPLETE)
                                .meetId(event.getMeetId())
                                .reviewId(event.getReviewId())
                                .payload(event.payload())
                                .userId(u.getId())
                                .build()
                )
                .toList();
    }
}
