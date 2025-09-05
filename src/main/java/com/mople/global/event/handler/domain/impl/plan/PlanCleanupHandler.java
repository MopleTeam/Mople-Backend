package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanCleanupHandler implements DomainEventHandler<PlanSoftDeletedEvent> {

    private final PlanParticipantRepository participantRepository;

    @Override
    public Class<PlanSoftDeletedEvent> getHandledType() {
        return PlanSoftDeletedEvent.class;
    }

    @Override
    public void handle(PlanSoftDeletedEvent event) {
        participantRepository.deleteByPlanId(event.getPlanId());
    }
}
