package com.mople.global.event.data.handler.notify.impl.review;

import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.notification.Notification;
import com.mople.entity.user.User;
import com.mople.global.event.data.handler.notify.NotifyEventHandler;
import com.mople.notification.utils.NotifySendRequestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Action.COMPLETE;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyHandler implements NotifyEventHandler<ReviewUpdateNotifyEvent> {

    private final NotifySendRequestFactory requestFactory;

    @Override
    public Class<ReviewUpdateNotifyEvent> getHandledType() {
        return ReviewUpdateNotifyEvent.class;
    }

    @Override
    public NotifySendRequest getSendRequest(ReviewUpdateNotifyEvent event) {
        return requestFactory.getReviewPushToken(event.getReviewUpdatedBy(), event.getReviewId(), event.notifyType().getTopic());
    }

    @Override
    public List<Notification> getNotifications(ReviewUpdateNotifyEvent event, List<User> users) {
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
