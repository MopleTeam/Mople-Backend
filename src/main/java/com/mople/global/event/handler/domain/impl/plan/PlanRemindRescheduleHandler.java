package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.domain.plan.PlanTimeChangedEvent;
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
public class PlanRemindRescheduleHandler implements DomainEventHandler<PlanTimeChangedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<PlanTimeChangedEvent> getHandledType() {
        return PlanTimeChangedEvent.class;
    }

    @Override
    public void handle(PlanTimeChangedEvent event) {
        outboxService.cancel(PLAN_REMIND, PLAN, event.planId());

        if (event.newTime().isAfter(LocalDateTime.now().plusHours(1))) {
            long hour = event.newTime().until(event.oldTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;
            LocalDateTime runAt = event.newTime().minusHours(hour).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

            PlanRemindEvent remindEvent = PlanRemindEvent.builder()
                    .planId(event.planId())
                    .build();

            outboxService.saveWithRunAt(PLAN_REMIND, PLAN, event.planId(), runAt, remindEvent);
        }
    }
}
