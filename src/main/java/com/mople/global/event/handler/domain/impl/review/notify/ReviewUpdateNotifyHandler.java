package com.mople.global.event.handler.domain.impl.review.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewUpdatedEvent;
import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyHandler implements DomainEventHandler<ReviewUpdatedEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<ReviewUpdatedEvent> supports() {
        return ReviewUpdatedEvent.class;
    }

    @Override
    public void handle(ReviewUpdatedEvent event) {
        if (!event.isFirstUpload()) {
            return;
        }

        PlanReview review = reviewRepository.findById(event.getReviewId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        Meet meet = meetRepository.findById(review.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewUpdateNotifyEvent notifyEvent = ReviewUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(event.getReviewId())
                .reviewName(review.getName())
                .reviewUpdatedBy(event.getReviewUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
