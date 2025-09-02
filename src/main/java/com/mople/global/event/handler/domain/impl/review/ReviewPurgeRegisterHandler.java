package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.review.ReviewPurgeEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.REVIEW_PURGE;

@Component
@RequiredArgsConstructor
public class ReviewPurgeRegisterHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<ReviewSoftDeletedEvent> getHandledType() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        LocalDateTime runAt = LocalDateTime.now().plusDays(7);

        ReviewPurgeEvent purgeEvent = ReviewPurgeEvent.builder()
                .reviewId(event.getReviewId())
                .build();

        outboxService.saveWithRunAt(REVIEW_PURGE, REVIEW, event.getReviewId(), runAt, purgeEvent);
    }
}
