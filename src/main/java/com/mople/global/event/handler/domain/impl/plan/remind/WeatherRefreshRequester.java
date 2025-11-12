package com.mople.global.event.handler.domain.impl.plan.remind;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.global.WeatherRefreshRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.WEATHER_REFRESH_REQUESTED;

@Component
@RequiredArgsConstructor
public class WeatherRefreshRequester implements DomainEventHandler<PlanRemindEvent> {

    private final MeetPlanRepository planRepository;
    private final OutboxService outboxService;

    @Override
    public Class<PlanRemindEvent> getHandledType() {
        return PlanRemindEvent.class;
    }

    @Override
    public void handle(PlanRemindEvent event) {
        MeetPlan plan = planRepository.findByIdAndStatus(event.planId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        if (!plan.hasLocation()) {
            return;
        }

        WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                .planId(event.planId())
                .build();

        outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, event.planId(), requestedEvent);
    }
}
