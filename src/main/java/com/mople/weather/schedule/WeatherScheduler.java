package com.mople.weather.schedule;

import com.mople.dto.event.data.domain.global.WeatherRefreshRequestedEvent;
import com.mople.meet.repository.impl.plan.PlanRepositorySupport;
import com.mople.outbox.service.OutboxService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.event.AggregateType.PLAN;
import static com.mople.global.enums.event.EventTypeNames.WEATHER_REFRESH_REQUESTED;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final PlanRepositorySupport support;
    private final OutboxService outboxService;

    @Async("taskSchedule")
    @Scheduled(cron = "${cron.weather.update}", zone = "Asia/Seoul")
    public void updateMeetingPlanWeatherInfo() {
        List<Long> updateWeatherPlanIds = support.findUpdateWeatherPlan();

        chunk(updateWeatherPlanIds, ids ->
                ids.forEach((id) -> {
                    WeatherRefreshRequestedEvent requestedEvent = WeatherRefreshRequestedEvent.builder()
                            .planId(id)
                            .build();

                    outboxService.save(WEATHER_REFRESH_REQUESTED, PLAN, id, requestedEvent);
                })
        );
    }
}