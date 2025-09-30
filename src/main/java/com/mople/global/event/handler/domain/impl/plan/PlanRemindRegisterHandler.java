package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanCreatedEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.PLAN_REMIND;

@Component
@RequiredArgsConstructor
public class PlanRemindRegisterHandler implements DomainEventHandler<PlanCreatedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<PlanCreatedEvent> getHandledType() {
        return PlanCreatedEvent.class;
    }

    @Override
    public void handle(PlanCreatedEvent event) {
        LocalDateTime now = LocalDateTime.now();

        if (event.planTime().isBefore(now.plusHours(1))) {
            return;
        }

        long hour = now.until(event.planTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;
        LocalDateTime runAt = event.planTime().minusHours(hour).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

        PlanRemindEvent remindEvent = PlanRemindEvent.builder()
                .planId(event.planId())
                .build();

        outboxService.saveWithRunAt(PLAN_REMIND, PLAN, event.planId(), runAt, remindEvent);
    }
}
