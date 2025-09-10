package com.mople.global.event.handler.domain.impl.plan.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanTimeChangedEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.dto.response.notification.NotificationSnapshot;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifier implements DomainEventHandler<PlanTimeChangedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<PlanTimeChangedEvent> getHandledType() {
        return PlanTimeChangedEvent.class;
    }

    @Override
    public void handle(PlanTimeChangedEvent event) {
        List<Long> targetIds = userReader.findPlanUsersNoTriggers(event.timeChangedBy(), event.planId());

        if (targetIds.isEmpty()) {
            return;
        }

        MeetPlan plan = planRepository.findByIdAndStatus(event.planId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanUpdateNotifyEvent notifyEvent = PlanUpdateNotifyEvent.builder()
                .meetName(meet.getName())
                .planId(plan.getId())
                .planName(plan.getName())
                .build();

        NotifyRequestedEvent requestedEvent = NotifyRequestedEvent.builder()
                .notifyType(notifyEvent.notifyType())
                .snapshot(
                        NotificationSnapshot.builder()
                                .payload(notifyEvent.payload())
                                .meetId(meet.getId())
                                .planId(plan.getId())
                                .reviewId(null)
                                .build()
                )
                .targetIds(targetIds)
                .routing(notifyEvent.routing())
                .build();

        outboxService.save(NOTIFY_REQUESTED, PLAN, plan.getId(), requestedEvent);
    }
}
