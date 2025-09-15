package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.review.ReviewPurgeEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.review.PlanReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewPurgeHandler implements DomainEventHandler<ReviewPurgeEvent> {

    private final PlanReviewRepository reviewRepository;

    @Override
    public Class<ReviewPurgeEvent> getHandledType() {
        return ReviewPurgeEvent.class;
    }

    @Override
    public void handle(ReviewPurgeEvent event) {
        reviewRepository.hardDeleteById(event.reviewId());
    }
}
