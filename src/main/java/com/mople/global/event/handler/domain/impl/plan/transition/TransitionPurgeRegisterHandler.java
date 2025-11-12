package com.mople.global.event.handler.domain.impl.plan.transition;

import com.mople.dto.event.data.domain.plan.PlanPurgeEvent;
import com.mople.dto.event.data.domain.plan.PlanTransitionedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.PLAN_PURGE;

@Component
@RequiredArgsConstructor
public class TransitionPurgeRegisterHandler implements DomainEventHandler<PlanTransitionedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<PlanTransitionedEvent> getHandledType() {
        return PlanTransitionedEvent.class;
    }

    @Override
    public void handle(PlanTransitionedEvent event) {
        LocalDateTime runAt = LocalDateTime.now().plusDays(14);

        PlanPurgeEvent purgeEvent = PlanPurgeEvent.builder()
                .planId(event.planId())
                .build();

        outboxService.saveWithRunAt(PLAN_PURGE, PLAN, event.planId(), runAt, purgeEvent);
    }
}
