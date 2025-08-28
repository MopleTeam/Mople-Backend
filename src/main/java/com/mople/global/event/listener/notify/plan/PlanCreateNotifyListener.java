package com.mople.global.event.listener.notify.plan;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.event.data.notify.plan.PlanCreateNotifyEvent;
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
public class PlanCreateNotifyListener {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository meetPlanRepository;
    private final NotificationSendService sendService;

    @EventListener
    public void pushEventListener(PlanCreateEvent event) {
        MeetPlan plan = meetPlanRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanCreateNotifyEvent notifyEvent = PlanCreateNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(plan.getId())
                .planName(plan.getName())
                .planTime(plan.getPlanTime())
                .planCreatorId(plan.getCreatorId())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }
}
