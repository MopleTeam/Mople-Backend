package com.mople.global.event.handler.domain.impl.plan.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanCreatedEvent;
import com.mople.dto.event.data.notify.plan.PlanCreateNotifyEvent;
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
public class PlanCreatedNotifyHandler implements DomainEventHandler<PlanCreatedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanCreatedEvent> supports() {
        return PlanCreatedEvent.class;
    }

    @Override
    public void handle(PlanCreatedEvent event) {
        MeetPlan plan = planRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanCreateNotifyEvent notifyEvent = PlanCreateNotifyEvent.builder()
                .meetId(plan.getMeetId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .planTime(plan.getPlanTime())
                .planCreatorId(event.getPlanCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
