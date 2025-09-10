package com.mople.global.event.handler.domain.impl.review.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.dto.event.data.notify.review.ReviewRemindNotifyEvent;
import com.mople.dto.response.notification.NotificationSnapshot;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.review.PlanReview;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class ReviewRemindNotifier implements DomainEventHandler<ReviewRemindEvent> {

    private final MeetRepository meetRepository;
    private final PlanReviewRepository reviewRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<ReviewRemindEvent> getHandledType() {
        return ReviewRemindEvent.class;
    }

    @Override
    public void handle(ReviewRemindEvent event) {
        PlanReview review = reviewRepository.findByIdAndStatus(event.reviewId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_REVIEW));

        if (review.getUpload()) {
            return;
        }

        List<Long> targetIds = userReader.findReviewCreator(review.getCreatorId());

        if (targetIds.isEmpty()) {
            return;
        }

        Meet meet = meetRepository.findByIdAndStatus(review.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        ReviewRemindNotifyEvent notifyEvent = ReviewRemindNotifyEvent.builder()
                .meetName(meet.getName())
                .reviewId(review.getId())
                .reviewName(review.getName())
                .build();

        NotifyRequestedEvent requestedEvent = NotifyRequestedEvent.builder()
                .notifyType(notifyEvent.notifyType())
                .snapshot(
                        NotificationSnapshot.builder()
                                .payload(notifyEvent.payload())
                                .meetId(meet.getId())
                                .planId(null)
                                .reviewId(review.getId())
                                .build()
                )
                .targetIds(targetIds)
                .routing(notifyEvent.routing())
                .build();

        outboxService.save(NOTIFY_REQUESTED, REVIEW, review.getId(), requestedEvent);
    }
}
