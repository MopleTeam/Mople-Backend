package com.mople.meet.schedule;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoScheduleResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.notification.Notification;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.mople.global.enums.Action.PENDING;

@Component
@RequiredArgsConstructor
public class PlanReminderWorker {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final WeatherService weatherService;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanParticipantRepository participantRepository;
    private final NotificationRepository notificationRepository;

    private final PlanReminderScheduleJob scheduleJob;
    private final NotificationSendService sendService;

    @Transactional
    public void runPlanReminder(Long planId, LocalDateTime time, Long userId, String meetName) {
        MeetPlan plan = meetPlanRepository.findById(planId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_PLAN)
                );

        List<Notification> planRemindNotification =
                notificationRepository.findPlanRemindNotification(planId, PENDING);

        if (planRemindNotification.isEmpty()) {
            List<PlanParticipant> participants = participantRepository.findParticipantsByPlanId(planId);
            List<Notification> notifications = participants.stream()
                    .map(p -> Notification.builder()
                            .planId(planId)
                            .meetId(plan.getMeetId())
                            .action(PENDING)
                            .userId(p.getUserId())
                            .scheduledAt(time)
                            .build()
                    )
                    .toList();

            notificationRepository.saveAll(notifications);
        }

        LocalDateTime planTime = plan.getPlanTime().atZone(KST).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime requestTime = time.atZone(KST).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);

        if (planTime.isBefore(requestTime)) {
            return;
        }

        if (!planTime.equals(requestTime)) {
            scheduleJob.scheduleReminder(plan.getId(), plan.getPlanTime(), userId, meetName);
            return;
        }

        WeatherInfoScheduleResponse weather =
                new WeatherInfoScheduleResponse(weatherInfo(plan.getLongitude(), plan.getLatitude()));

        PlanRemindNotifyEvent event = PlanRemindNotifyEvent.builder()
                .meetId(plan.getMeetId())
                .meetName(meetName)
                .planId(plan.getId())
                .planName(plan.getName())
                .planTime(plan.getPlanTime())
                .planCreatorId(userId)
                .temperature(weather.temperature())
                .iconImage(weather.weatherIconImage())
                .build();

        sendService.sendMultiNotification(event);
    }

    private OpenWeatherResponse weatherInfo(BigDecimal lot, BigDecimal lat) {
        return weatherService.getWeatherInfoByLocation(new CoordinateRequest(lot, lat)).join();
    }
}
