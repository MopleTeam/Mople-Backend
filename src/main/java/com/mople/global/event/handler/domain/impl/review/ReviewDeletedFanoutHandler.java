package com.mople.global.event.handler.domain.impl.review;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Status.DELETED;
import static com.mople.global.enums.event.AggregateType.POST;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;

@Component
@RequiredArgsConstructor
public class ReviewDeletedFanoutHandler implements DomainEventHandler<ReviewSoftDeletedEvent> {

    private final PlanCommentRepository commentRepository;
    private final OutboxService outboxService;

    @Override
    public Class<ReviewSoftDeletedEvent> getHandledType() {
        return ReviewSoftDeletedEvent.class;
    }

    @Override
    public void handle(ReviewSoftDeletedEvent event) {
        List<Long> commentIds = commentRepository.findIdsByPostIdAndStatus(event.getPlanId(), Status.ACTIVE);
        commentRepository.softDeleteAll(DELETED, commentIds, event.getReviewDeletedBy());

        CommentsSoftDeletedEvent deleteEvent = CommentsSoftDeletedEvent.builder()
                .postId(event.getPlanId())
                .commentIds(commentIds)
                .commentsDeletedBy(event.getReviewDeletedBy())
                .build();

        outboxService.save(COMMENTS_SOFT_DELETED, POST, event.getPlanId(), deleteEvent);
    }
}
