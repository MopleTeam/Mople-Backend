package com.mople.global.event.data.handler.domain.impl.review;

import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.event.data.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.REVIEW_REMIND;

@Component
@RequiredArgsConstructor
public class ReviewRemindCancelHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<ReviewSoftDeletedEvent> supports() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        outboxService.cancel(REVIEW_REMIND, REVIEW, event.getReviewId());
    }
}
