package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.dto.event.data.domain.review.ReviewSoftDeletedEvent;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Status.DELETED;
import static com.mople.global.enums.event.AggregateType.*;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class MeetDeletedFanoutHandler implements DomainEventHandler<MeetSoftDeletedEvent> {

    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final OutboxService outboxService;

    @Override
    public Class<MeetSoftDeletedEvent> getHandledType() {
        return MeetSoftDeletedEvent.class;
    }

    @Override
    public void handle(MeetSoftDeletedEvent event) {
        List<Long> planIds = planRepository.findIdsByMeetId(event.getMeetId());
        List<Long> reviewIds = reviewRepository.findIdsByMeetId(event.getMeetId());

        chunk(planIds, ids -> {
            planRepository.softDeleteAll(DELETED, ids, event.getMeetDeletedBy());

            ids.forEach(id -> {
                PlanSoftDeletedEvent deleteEvent = PlanSoftDeletedEvent.builder()
                        .planId(id)
                        .planDeletedBy(event.getMeetDeletedBy())
                        .cause(DeletionCause.CASCADE_FROM_MEET_DELETE)
                        .build();

                outboxService.save(PLAN_SOFT_DELETED, PLAN, id, deleteEvent);
            });
        });

        chunk(reviewIds, ids -> {
            reviewRepository.softDeleteAll(DELETED, ids, event.getMeetDeletedBy());

            ids.forEach(id -> {
                ReviewSoftDeletedEvent deleteEvent = ReviewSoftDeletedEvent.builder()
                        .planId(reviewRepository.findPlanIdById(id))
                        .reviewId(id)
                        .reviewDeletedBy(event.getMeetDeletedBy())
                        .build();

                outboxService.save(REVIEW_SOFT_DELETED, REVIEW, id, deleteEvent);
            });
        });
    }
}
