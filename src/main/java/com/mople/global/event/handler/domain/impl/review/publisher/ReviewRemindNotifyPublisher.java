package com.mople.global.event.handler.domain.impl.review.publisher;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
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
public class ReviewRemindNotifyPublisher implements DomainEventHandler<ReviewRemindEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<ReviewRemindEvent> getHandledType() {
        return ReviewRemindEvent.class;
    }

    @Override
    public void handle(ReviewRemindEvent event) {
        PlanReview review = reviewRepository.findById(event.getReviewId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        if (review.getUpload()) {
            return;
        }

        Meet meet = meetRepository.findById(review.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewRemindNotifyEvent notifyEvent = ReviewRemindNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(event.getReviewId())
                .reviewName(review.getName())
                .reviewCreatorId(review.getCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
