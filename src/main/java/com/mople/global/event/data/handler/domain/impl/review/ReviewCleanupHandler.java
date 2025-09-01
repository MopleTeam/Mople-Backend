package com.mople.global.event.data.handler.domain.impl.review;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.event.data.handler.domain.DomainEventHandler;
import com.mople.image.service.ImageService;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.ReviewImageRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.REVIEW;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;

@Component
@RequiredArgsConstructor
public class ReviewCleanupHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final PlanCommentRepository commentRepository;
    private final PlanParticipantRepository participantRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ImageService imageService;
    private final OutboxService outboxService;

    @Override
    public Class<ReviewSoftDeletedEvent> supports() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        List<String> reviewImages = reviewImageRepository.findReviewImagesByReviewId(event.getReviewId());

        participantRepository.deleteByReviewId(event.getReviewId());
        reviewImageRepository.deleteByReviewId(event.getReviewId());

        reviewImages.forEach(imageService::deleteImage);

        commentRepository.softDeleteAll(commentRepository.findIdsByPostId(event.getPlanId()), event.getReviewDeletedBy());

        CommentsSoftDeletedEvent deleteEvent = CommentsSoftDeletedEvent.builder()
                .postId(event.getPlanId())
                .commentsDeletedBy(event.getReviewDeletedBy())
                .build();

        outboxService.save(COMMENTS_SOFT_DELETED, REVIEW, event.getReviewId(), deleteEvent);
    }
}
