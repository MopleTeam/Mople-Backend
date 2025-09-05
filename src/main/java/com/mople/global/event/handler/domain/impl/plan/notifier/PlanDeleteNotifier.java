package com.mople.global.event.handler.domain.impl.plan.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifier implements DomainEventHandler<PlanSoftDeletedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanSoftDeletedEvent> getHandledType() {
        return PlanSoftDeletedEvent.class;
    }

    @Override
    public void handle(PlanSoftDeletedEvent event) {
        if (event.getCause() != DeletionCause.DIRECT_PLAN_DELETE) {
            return;
        }

        List<Long> targetIds = userReader.findPlanUsersNoTriggers(event.getPlanDeletedBy(), event.getPlanId());

        MeetPlan plan = planRepository.findByIdAndStatus(event.getPlanId(), Status.DELETED)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanDeleteNotifyEvent notifyEvent = PlanDeleteNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .targetIds(targetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
