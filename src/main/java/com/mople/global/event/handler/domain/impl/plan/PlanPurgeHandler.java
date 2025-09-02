package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanPurgeEvent;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
        Status planStatus = planRepository.findStatusById(event.getPlanId());

        if (!Objects.equals(planStatus, Status.DELETED)) {
            return;
        }

        planRepository.deleteById(event.getPlanId());
    }
}
