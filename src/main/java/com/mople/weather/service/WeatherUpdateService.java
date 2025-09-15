package com.mople.weather.service;

import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.WeatherInfoResponse;
import com.mople.meet.repository.plan.MeetPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WeatherUpdateService {

    private final MeetPlanRepository planRepository;
    private final WeatherService weatherService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public WeatherInfoResponse fetch(CoordinateRequest coordinate, LocalDateTime planTime) {
        return weatherService.getClosestWeatherInfoFromDateTime(coordinate, planTime)
                .orTimeout(2, TimeUnit.SECONDS)
                .join();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void apply(Long planId, WeatherInfoResponse w) {
        planRepository.updateWeather(planId, w.temperature(), w.pop(), w.weatherIcon());
    }
}
