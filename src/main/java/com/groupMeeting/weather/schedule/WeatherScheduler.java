package com.groupMeeting.weather.schedule;

import com.groupMeeting.dto.request.weather.CoordinateRequest;
import com.groupMeeting.meet.repository.impl.plan.PlanRepositorySupport;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class WeatherScheduler {
    private final WeatherService openWeatherService;
    private final MeetPlanRepository planRepository;
    private final PlanRepositorySupport support;

    @Async("taskSchedule")
    @Scheduled(cron = "0 0 */6 * * *")
    public void updateMeetingPlanWeatherInfo() {
        var plans = support.findUpdateWeatherPlan();

        plans.forEach(meetingPlan -> {
            openWeatherService.getClosestWeatherInfoFromDateTime(
                    new CoordinateRequest(meetingPlan.getLongitude(), meetingPlan.getLatitude()),
                    meetingPlan.getPlanTime()
            ).thenAccept(weatherInfo -> {
                if (isNull(weatherInfo)) return;

                meetingPlan.updateWeather(weatherInfo);
                planRepository.save(meetingPlan);
            });
        });
    }
}