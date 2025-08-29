package com.mople.global.event.listener.notify.review;

import com.mople.dto.event.data.domain.review.ReviewUpdateEvent;
import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyListener {

    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(ReviewUpdateEvent event) {
        if (!event.isFirstUpload()) {
            return;
        }

        ReviewUpdateNotifyEvent notifyEvent = ReviewUpdateNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(event.getMeetName())
                .reviewId(event.getReviewId())
                .reviewName(event.getReviewName())
                .reviewUpdatedBy(event.getReviewUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
