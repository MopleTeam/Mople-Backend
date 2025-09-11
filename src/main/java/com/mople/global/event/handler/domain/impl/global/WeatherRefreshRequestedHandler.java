package com.mople.global.event.handler.domain.impl.global;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.global.WeatherRefreshRequestedEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.WeatherInfoResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.weather.service.WeatherUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherRefreshRequestedHandler implements DomainEventHandler<WeatherRefreshRequestedEvent> {

    private final MeetPlanRepository planRepository;
    private final WeatherUpdateService weatherUpdateService;

    @Override
    public Class<WeatherRefreshRequestedEvent> getHandledType() {
        return WeatherRefreshRequestedEvent.class;
    }

    @Override
    public void handle(WeatherRefreshRequestedEvent event) {
        MeetPlan plan = planRepository.findByIdAndStatus(event.planId(), Status.ACTIVE)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_PLAN));

        CoordinateRequest coordinate = new CoordinateRequest(plan.getLongitude(), plan.getLatitude());
        WeatherInfoResponse weatherInfo = weatherUpdateService.fetch(coordinate, plan.getPlanTime());

        System.out.println(weatherInfo.temperature());
        System.out.println(weatherInfo.pop());

        weatherUpdateService.apply(plan.getId(), weatherInfo);
    }
}
