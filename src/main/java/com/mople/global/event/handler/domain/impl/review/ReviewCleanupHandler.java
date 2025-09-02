package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.image.service.ImageService;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.ReviewImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewCleanupHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final PlanParticipantRepository participantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImageService imageService;

    @Override
    public Class<ReviewSoftDeletedEvent> getHandledType() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        List<String> reviewImages = reviewImageRepository.findReviewImagesByReviewId(event.getReviewId());

        participantRepository.deleteByReviewId(event.getReviewId());
        reviewImageRepository.deleteByReviewId(event.getReviewId());

        reviewImages.forEach(imageService::deleteImage);
    }
}
