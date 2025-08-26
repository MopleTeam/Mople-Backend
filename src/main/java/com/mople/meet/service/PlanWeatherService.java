package com.mople.meet.service;

import com.mople.dto.response.weather.WeatherInfoResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.meet.repository.plan.MeetPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanWeatherService {

    private final MeetPlanRepository planRepository;

    @Transactional
    public void applyWeather(Long planId, WeatherInfoResponse response) {
        MeetPlan plan = planRepository.findById(planId).orElse(null);
        if (plan == null) return;

        plan.updateWeather(response);
    }
}
