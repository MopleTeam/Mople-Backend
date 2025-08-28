package com.mople.global.event.listener.notify.plan;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanUpdateEvent;
import com.mople.dto.event.data.notify.plan.PlanUpdateNotifyEvent;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.service.NotificationSendService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanUpdateNotifyListener {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository meetPlanRepository;
    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(PlanUpdateEvent event) {
        MeetPlan plan = meetPlanRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanUpdateNotifyEvent notifyEvent = PlanUpdateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(plan.getId())
                .planName(plan.getName())
                .planUpdatedBy(event.getPlanUpdatedBy())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
