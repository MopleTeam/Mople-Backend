package com.mople.global.event.listener.domain.plan;

import com.mople.dto.event.data.domain.plan.PlanCreateEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.WeatherInfoResponse;
import com.mople.meet.service.PlanWeatherService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PlanCreateWeatherListener {

    private final WeatherService weatherService;
    private final PlanWeatherService planWeatherService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pushEventListener(PlanCreateEvent event) {
        LocalDateTime now = LocalDateTime.now();

        if (event.planTime().isAfter(now.plusDays(5))) return;

        WeatherInfoResponse response = weatherService
                .getClosestWeatherInfoFromDateTime(
                        new CoordinateRequest(event.lot(), event.lat()),
                        event.planTime()
                )
                .exceptionally(t -> null)
                .thenApply(weatherInfo -> weatherInfo)
                .join();

        planWeatherService.applyWeather(event.planId(), response);
    }
}
