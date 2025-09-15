package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanTransitionRequestedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.service.plan.PlanTransitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanTransitionRequestedHandler implements DomainEventHandler<PlanTransitionRequestedEvent> {

    private final PlanTransitionService transitionService;

    @Override
    public Class<PlanTransitionRequestedEvent> getHandledType() {
        return PlanTransitionRequestedEvent.class;
    }

    @Override
    public void handle(PlanTransitionRequestedEvent event) {
        transitionService.transitionPlanByOne(event.planId());
    }
}
