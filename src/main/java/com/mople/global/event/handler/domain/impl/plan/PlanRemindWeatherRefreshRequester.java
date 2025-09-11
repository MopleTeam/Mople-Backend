package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.global.WeatherRefreshRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.WEATHER_REFRESH_REQUESTED;

@Component
@RequiredArgsConstructor
public class PlanRemindWeatherRefreshRequester implements DomainEventHandler<PlanRemindEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<PlanRemindEvent> getHandledType() {
        return PlanRemindEvent.class;
    }

    @Override
    public void handle(PlanRemindEvent event) {
        WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                .planId(event.planId())
                .build();

        outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, event.planId(), requestedEvent);
    }
}
