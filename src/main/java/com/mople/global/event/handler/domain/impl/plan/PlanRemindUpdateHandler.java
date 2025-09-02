package com.mople.global.event.handler.domain.impl.plan;

import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.domain.plan.PlanUpdatedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.PLAN_REMIND;

@Component
@RequiredArgsConstructor
public class PlanRemindUpdateHandler implements DomainEventHandler<PlanUpdatedEvent> {

    private final OutboxService outboxService;

    @Override
    public Class<PlanUpdatedEvent> supports() {
        return PlanUpdatedEvent.class;
    }

    @Override
    public void handle(PlanUpdatedEvent event) {
        if (Objects.equals(event.getNewTime(), event.getPreTime())) {
            return;
        }

        outboxService.cancel(PLAN_REMIND, PLAN, event.getPlanId());

        if (event.getNewTime().isAfter(LocalDateTime.now().plusHours(1))) {
            long hour = event.getNewTime().until(event.getPreTime(), ChronoUnit.HOURS) == 1 ? 1 : 2;
            LocalDateTime runAt = event.getNewTime().minusHours(hour).atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();

            PlanRemindEvent remindEvent = PlanRemindEvent.builder()
                    .planId(event.getPlanId())
                    .build();

            outboxService.saveWithRunAt(PLAN_REMIND, PLAN, event.getPlanId(), runAt, remindEvent);
        }
    }
}
