package com.mople.global.event.handler.domain.impl.review.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.review.ReviewUploadEvent;
import com.mople.dto.event.data.notify.review.ReviewUploadNotifyEvent;
import com.mople.dto.response.notification.NotificationSnapshot;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.repository.NotificationRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class ReviewUploadNotifier implements DomainEventHandler<ReviewUploadEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

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
                .meetName(meet.getName())
                .reviewId(review.getId())
                .reviewName(review.getName())
                .build();

        List<Long> notificationIds = notificationRepository.saveAll(
                        targetIds.stream()
                                .map(targetId ->
                                        Notification.builder()
                                                .type(notifyEvent.notifyType())
                                                .meetId(meet.getId())
                                                .reviewId(review.getId())
                                                .payload(notifyEvent.payload())
                                                .userId(targetId)
                                                .build()
                                )
                                .toList()
                ).stream()
                .map(Notification::getId).toList();

        outboxService.save(
                NOTIFY_REQUESTED,
                REVIEW,
                review.getId(),
                new NotifyRequestedEvent(notifyEvent, notificationIds)
        );
    }
}
