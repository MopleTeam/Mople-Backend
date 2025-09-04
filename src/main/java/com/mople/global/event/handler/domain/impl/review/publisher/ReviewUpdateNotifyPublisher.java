package com.mople.global.event.handler.domain.impl.review.publisher;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewUpdatedEvent;
import com.mople.dto.event.data.notify.review.ReviewUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewUpdateNotifyPublisher implements DomainEventHandler<ReviewUpdatedEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<ReviewUpdatedEvent> getHandledType() {
        return ReviewUpdatedEvent.class;
    }

    @Override
    public void handle(ReviewUpdatedEvent event) {
        if (!event.isFirstUpload()) {
            return;
        }

        PlanReview review = reviewRepository.findByIdAndStatus(event.getReviewId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        List<Long> targetIds = userReader.findReviewUsersNoTriggers(event.getReviewUpdatedBy(), event.getReviewId());

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewUpdateNotifyEvent notifyEvent = ReviewUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(event.getReviewId())
                .reviewName(review.getName())
                .reviewUpdatedBy(event.getReviewUpdatedBy())
                .targetIds(targetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
