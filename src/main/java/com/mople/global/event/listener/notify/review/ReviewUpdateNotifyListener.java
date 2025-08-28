package com.mople.global.event.listener.notify.review;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewUpdateEvent;
import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyListener {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;
    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(ReviewUpdateEvent event) {
        if (!event.isFirstUpload()) {
            return;
        }

        PlanReview review = reviewRepository.findById(event.getReviewId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(review.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewUpdateNotifyEvent notifyEvent = ReviewUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(review.getId())
                .reviewName(review.getName())
                .reviewUpdatedBy(event.getReviewUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
