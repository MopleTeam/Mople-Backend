package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.meet.MeetLeftEvent;
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

import static com.mople.global.enums.event.AggregateType.*;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class MeetLeftFanoutHandler implements DomainEventHandler<MeetLeftEvent> {

    private static final int CHUNK = 800;

    private final MeetPlanRepository planRepository;
    private final PlanReviewRepository reviewRepository;
    private final OutboxService outboxService;

    @Override
    public Class<MeetLeftEvent> getHandledType() {
        return MeetLeftEvent.class;
    }

    @Override
    public void handle(MeetLeftEvent event) {
        List<Long> ownedPlanIds = planRepository.findIdsByMeetIdAndCreatorId(event.getMeetId(), event.getLeaveMemberId());
        List<Long> ownedReviewIds = reviewRepository.findIdsByMeetIdAndCreatorId(event.getMeetId(), event.getLeaveMemberId());

        chunk(ownedPlanIds, CHUNK, ids -> {
            planRepository.softDeleteAll(ids, event.getLeaveMemberId());

            ids.forEach(id -> {
                PlanSoftDeletedEvent deleteEvent = PlanSoftDeletedEvent.builder()
                        .meetId(event.getMeetId())
                        .planId(id)
                        .planDeletedBy(event.getLeaveMemberId())
                        .cause(DeletionCause.CASCADE_FROM_MEET_LEAVE)
                        .build();

                outboxService.save(PLAN_SOFT_DELETED, PLAN, id, deleteEvent);
            });
        });

        chunk(ownedReviewIds, CHUNK, ids -> {
            reviewRepository.softDeleteAll(ids, event.getLeaveMemberId());

            ids.forEach(id -> {
                ReviewSoftDeletedEvent deleteEvent = ReviewSoftDeletedEvent.builder()
                        .meetId(event.getMeetId())
                        .planId(reviewRepository.findPlanIdById(id))
                        .reviewId(id)
                        .reviewDeletedBy(event.getLeaveMemberId())
                        .build();

                outboxService.save(REVIEW_SOFT_DELETED, REVIEW, id, deleteEvent);
            });
        });
    }
}
