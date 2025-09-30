package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanPurgeEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanPurgeHandler implements DomainEventHandler<PlanPurgeEvent> {

    private final MeetPlanRepository planRepository;

    @Override
    public Class<PlanPurgeEvent> getHandledType() {
        return PlanPurgeEvent.class;
    }

    @Override
    public void handle(PlanPurgeEvent event) {
        planRepository.hardDeleteById(event.planId());
    }
}
