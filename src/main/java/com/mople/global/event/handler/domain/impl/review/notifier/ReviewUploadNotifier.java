package com.mople.global.event.handler.domain.impl.review.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.review.ReviewUploadEvent;
import com.mople.dto.event.data.notify.review.ReviewUploadNotifyEvent;
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
public class ReviewUploadNotifier implements DomainEventHandler<ReviewUploadEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<ReviewUploadEvent> getHandledType() {
        return ReviewUploadEvent.class;
    }

    @Override
    public void handle(ReviewUploadEvent event) {
        PlanReview review = reviewRepository.findByIdAndStatus(event.reviewId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        List<Long> targetIds = userReader.findReviewUsersNoTriggers(event.reviewUpdatedBy(), event.reviewId());

        if (targetIds.isEmpty()) {
            return;
        }

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewUploadNotifyEvent notifyEvent = ReviewUploadNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .reviewId(event.reviewId())
                .reviewName(review.getName())
                .reviewUpdatedBy(event.reviewUpdatedBy())
                .targetIds(targetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
