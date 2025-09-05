package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewImageRemoveEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class ReviewImageRemoveHandler implements DomainEventHandler<ReviewImageRemoveEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<ReviewImageRemoveEvent> getHandledType() {
        return ReviewImageRemoveEvent.class;
    }

    @Override
    public void handle(ReviewImageRemoveEvent event) {
        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(REVIEW)
                .aggregateId(event.getReviewId())
                .imageUrl(event.getImageUrl())
                .imageDeletedBy(event.getImageDeletedBy())
                .build();

        outboxService.save(IMAGE_DELETED, REVIEW, event.getReviewId(), deletedEvent);
    }
}
