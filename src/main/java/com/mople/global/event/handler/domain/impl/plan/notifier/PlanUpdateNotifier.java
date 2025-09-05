package com.mople.global.event.handler.domain.impl.plan.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanTimeChangedEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
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
public class PlanUpdateNotifier implements DomainEventHandler<PlanTimeChangedEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;

    private final NotificationUserReader userReader;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanTimeChangedEvent> getHandledType() {
        return PlanTimeChangedEvent.class;
    }

    @Override
    public void handle(PlanTimeChangedEvent event) {
        List<Long> targetIds = userReader.findPlanUsersNoTriggers(event.getTimeChangedBy(), event.getPlanId());

        MeetPlan plan = planRepository.findByIdAndStatus(event.getPlanId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanUpdateNotifyEvent notifyEvent = PlanUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .targetIds(targetIds)
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
