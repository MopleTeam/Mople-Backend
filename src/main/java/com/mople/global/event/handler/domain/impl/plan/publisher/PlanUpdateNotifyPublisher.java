package com.mople.global.event.handler.domain.impl.plan.publisher;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanUpdatedEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifyPublisher implements DomainEventHandler<PlanUpdatedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanUpdatedEvent> getHandledType() {
        return PlanUpdatedEvent.class;
    }

    @Override
    public void handle(PlanUpdatedEvent event) {
        MeetPlan plan = planRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanUpdateNotifyEvent notifyEvent = PlanUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .planUpdatedBy(event.getPlanUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
