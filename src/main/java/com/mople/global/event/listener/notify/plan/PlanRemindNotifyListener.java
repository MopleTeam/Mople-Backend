package com.mople.global.event.listener.notify.plan;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoScheduleResponse;
import com.mople.entity.meet.Meet;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyListener {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository meetPlanRepository;
    private final NotificationSendService sendService;
    private final WeatherService weatherService;

    @EventListener
    @Transactional
    public void pushEventListener(PlanRemindEvent event) {
        MeetPlan plan = meetPlanRepository.findById(event.getPlanId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        Meet meet = meetRepository.findById(plan.getMeetId())
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        WeatherInfoScheduleResponse weather = new WeatherInfoScheduleResponse(
                weatherInfo(plan.getLongitude(), plan.getLatitude())
        );

        PlanRemindNotifyEvent notifyEvent = PlanRemindNotifyEvent.builder()
                .meetId(meet.getId())
                .meetName(meet.getName())
                .planId(plan.getId())
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
