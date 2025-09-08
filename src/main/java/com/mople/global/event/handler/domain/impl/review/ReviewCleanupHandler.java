package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.ReviewImageRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class ReviewCleanupHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final PlanParticipantRepository participantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final OutboxService outboxService;

    @Override
    public Class<ReviewSoftDeletedEvent> getHandledType() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        participantRepository.deleteByReviewId(event.reviewId());

        List<String> reviewImages = reviewImageRepository.findReviewImagesByReviewId(event.reviewId());

        if (reviewImages.isEmpty()) {
            return;
        }

        reviewImageRepository.deleteByReviewId(event.reviewId());

        reviewImages.forEach(i -> {
            ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                    .aggregateType(REVIEW)
                    .aggregateId(event.reviewId())
                    .imageUrl(i)
                    .imageDeletedBy(event.reviewDeletedBy())
                    .build();

            outboxService.save(IMAGE_DELETED, REVIEW, event.reviewId(), deletedEvent);
        });
    }
}
