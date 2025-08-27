package com.mople.global.event.listener.notify.review;

import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifyListener {

    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(ReviewRemindEvent event) {
        if (!event.isUpload()) {
            ReviewRemindNotifyEvent notifyEvent = ReviewRemindNotifyEvent.builder()
                    .meetId(event.getMeetId())
                    .meetName(event.getMeetName())
                    .reviewId(event.getReviewId())
                    .reviewName(event.getReviewName())
                    .reviewCreatorId(event.getReviewCreatorId())
                    .build();

            sendService.sendMultiNotification(notifyEvent);
        }
    }
}
