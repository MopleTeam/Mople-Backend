package com.mople.global.event.listener.notify.review;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
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
public class ReviewRemindNotifyListener {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;
    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(ReviewRemindEvent event) {
        PlanReview review = reviewRepository.findById(event.getReviewId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        Meet meet = meetRepository.findById(review.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        if (review.getUpload()) {
            return;
        }

        ReviewRemindNotifyEvent notifyEvent = ReviewRemindNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(review.getId())
                .reviewName(review.getName())
                .reviewCreatorId(review.getCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
