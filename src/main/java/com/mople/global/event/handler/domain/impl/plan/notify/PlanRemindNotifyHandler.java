package com.mople.global.event.handler.domain.impl.plan.notify;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoScheduleResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyHandler implements DomainEventHandler<PlanRemindEvent> {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository planRepository;
    private final WeatherService weatherService;
    private final NotificationSendService sendService;

    @Override
    public Class<PlanRemindEvent> supports() {
        return PlanRemindEvent.class;
    }

    @Override
    public void handle(PlanRemindEvent event) {
        MeetPlan plan = planRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        WeatherInfoScheduleResponse weather = new WeatherInfoScheduleResponse(
                weatherInfo(plan.getLongitude(), plan.getLatitude())
        );

        PlanRemindNotifyEvent notifyEvent = PlanRemindNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(event.getPlanId())
                .planName(plan.getName())
                .planTime(plan.getPlanTime())
                .planCreatorId(plan.getCreatorId())
                .temperature(weather.temperature())
                .iconImage(weather.weatherIconImage())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }

    private OpenWeatherResponse weatherInfo(BigDecimal lot, BigDecimal lat) {
        return weatherService.getWeatherInfoByLocation(new CoordinateRequest(lot, lat)).join();
    }
}
