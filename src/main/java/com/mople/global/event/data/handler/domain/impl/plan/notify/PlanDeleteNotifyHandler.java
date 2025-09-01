package com.mople.global.event.data.handler.domain.impl.plan.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanSoftDeletedEvent;
import com.mople.dto.event.data.notify.plan.PlanDeleteNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.event.DeletionCause;
import com.mople.global.event.data.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanDeleteNotifyHandler implements DomainEventHandler<PlanSoftDeletedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanSoftDeletedEvent> supports() {
        return PlanSoftDeletedEvent.class;
    }

    @Override
    public void handle(PlanSoftDeletedEvent event) {
        if (event.getCause() != DeletionCause.DIRECT_PLAN_DELETE) {
            return;
        }

        Meet meet = meetRepository.findById(event.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        MeetPlan plan = planRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        PlanDeleteNotifyEvent notifyEvent = PlanDeleteNotifyEvent.builder()
                .meetId(event.getMeetId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .planDeletedBy(event.getPlanDeletedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
