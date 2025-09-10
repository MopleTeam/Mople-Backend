package com.mople.global.event.handler.domain.impl.plan.notifier;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.notify.NotifyRequestedEvent;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.notification.NotificationSnapshot;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.outbox.service.OutboxService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.NOTIFY_REQUESTED;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifier implements DomainEventHandler<PlanRemindEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;

    private final WeatherService weatherService;
    private final NotificationUserReader userReader;
    private final OutboxService outboxService;

    @Override
    public Class<PlanRemindEvent> getHandledType() {
        return PlanRemindEvent.class;
    }

    @Override
    public void handle(PlanRemindEvent event) {
        MeetPlan plan = planRepository.findByIdAndStatus(event.planId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        List<Long> targetIds = userReader.findPlanUsersAll(plan.getCreatorId());

        if (targetIds.isEmpty()) {
            return;
        }

        Meet meet = meetRepository.findByIdAndStatus(plan.getMeetId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        PlanRemindNotifyEvent notifyEvent = PlanRemindNotifyEvent.builder()
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

    private OpenWeatherResponse weatherInfo(BigDecimal lot, BigDecimal lat) {
        return weatherService.getWeatherInfoByLocation(new CoordinateRequest(lot, lat)).join();
    }
}
