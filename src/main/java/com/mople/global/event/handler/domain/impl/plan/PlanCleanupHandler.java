package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.comment.CommentsSoftDeletedEvent;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.comment.PlanCommentRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.COMMENTS_SOFT_DELETED;

@Component
@RequiredArgsConstructor
public class PlanCleanupHandler implements DomainEventHandler<PlanSoftDeletedEvent> {

    private final PlanParticipantRepository participantRepository;
    private final PlanCommentRepository commentRepository;
    private final OutboxService outboxService;

    @Override
    public Class<PlanSoftDeletedEvent> getHandledType() {
        return PlanSoftDeletedEvent.class;
    }

    @Override
    public void handle(PlanSoftDeletedEvent event) {
        participantRepository.deleteByPlanId(event.getPlanId());
        commentRepository.softDeleteAll(commentRepository.findIdsByPostId(event.getPlanId()), event.getPlanDeletedBy());

        CommentsSoftDeletedEvent deleteEvent = CommentsSoftDeletedEvent.builder()
                .postId(event.getPlanId())
                .commentsDeletedBy(event.getPlanDeletedBy())
                .build();

        outboxService.save(COMMENTS_SOFT_DELETED, PLAN, event.getPlanId(), deleteEvent);
    }
}
