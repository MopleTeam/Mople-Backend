package com.mople.global.event.listener.domain.review;

import com.mople.dto.event.data.domain.review.ReviewCreateEvent;
import com.mople.dto.event.data.domain.review.ReviewRemindEvent;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static com.mople.global.enums.AggregateType.REVIEW;
import static com.mople.global.enums.EventTypeNames.REVIEW_REMIND;

@Component
@RequiredArgsConstructor
public class ReviewCreateReminderListener {

    private final OutboxService outboxService;

    @EventListener
    public void pushEventListener(ReviewCreateEvent event) {
        LocalDateTime noonKST = LocalDateTime
                .of(LocalDate.now(), LocalTime.of(12, 0, 0))
                .atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override
                public void afterCommit() {
                    publishReviewRemindEvent(event, noonKST);
                }
            });
        } else {
            publishReviewRemindEvent(event, noonKST);
        }
    }

    private void publishReviewRemindEvent(ReviewCreateEvent event, LocalDateTime runAt) {
        ReviewRemindEvent remindEvent = ReviewRemindEvent.builder()
                .meetId(event.getMeetId())
                .meetName(event.getMeetName())
                .reviewId(event.getReviewId())
                .reviewName(event.getReviewName())
                .reviewCreatorId(event.getReviewCreatorId())
                .isUpload(event.isUpload())
                .build();

        outboxService.saveWithRunAt(REVIEW_REMIND, REVIEW, event.getReviewId(), runAt, remindEvent);
    }
}
