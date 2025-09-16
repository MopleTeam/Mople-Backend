package com.mople.global.event.handler.domain.impl.plan.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.global.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.notification.Notification;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.repository.NotificationRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifier implements DomainEventHandler<PlanSoftDeletedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final NotificationRepository notificationRepository;

    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<PlanSoftDeletedEvent> getHandledType() {
        return PlanSoftDeletedEvent.class;
    }

    @Override
    public void handle(PlanSoftDeletedEvent event) {
        if (event.cause() != DeletionCause.DIRECT_PLAN_DELETE) {
            return;
        }

        List<Long> targetIds = userReader.findPlanUsersNoTriggers(event.planDeletedBy(), event.planId());

        if (targetIds.isEmpty()) {
            return;
        }

        MeetPlan plan = planRepository.findByIdAndStatus(event.planId(), Status.DELETED)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanDeleteNotifyEvent notifyEvent = PlanDeleteNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planName(plan.getName())
                .build();

        List<Long> notificationIds = notificationRepository.saveAll(
                        targetIds.stream()
                                .map(targetId ->
                                        Notification.builder()
                                                .type(notifyEvent.notifyType())
                                                .meetId(meet.getId())
                                                .planId(plan.getId())
                                                .payload(notifyEvent.payload())
                                                .userId(targetId)
                                                .build()
                                )
                                .toList()
                )
                .stream()
                .map(Notification::getId).toList();

        outboxService.save(
                NOTIFY_REQUESTED,
                PLAN,
                plan.getId(),
                new NotifyRequestedEvent(notifyEvent, notificationIds)
        );
    }
}
