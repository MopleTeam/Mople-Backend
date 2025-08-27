package com.mople.global.event.listener.notify.plan;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.domain.plan.PlanRemindEvent;
import com.mople.dto.event.data.notify.plan.PlanRemindNotifyEvent;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoScheduleResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.notification.Notification;
import com.mople.global.enums.Action;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.notification.repository.NotificationRepository;
import com.mople.notification.service.NotificationSendService;
import com.mople.outbox.service.OutboxService;
import com.mople.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.mople.global.enums.AggregateType.PLAN;
import static com.mople.global.enums.EventTypeNames.PLAN_REMIND;

@Component
@RequiredArgsConstructor
public class PlanRemindNotifyListener {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MeetPlanRepository meetPlanRepository;
    private final PlanParticipantRepository participantRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSendService sendService;
    private final OutboxService outboxService;
    private final WeatherService weatherService;

    @EventListener
    @Transactional
    public void pushEventListener(PlanRemindEvent event) {
        MeetPlan plan = meetPlanRepository.findById(event.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_PLAN));

        List<Notification> pendings = notificationRepository.findPlanRemindNotification(plan.getId(), Action.PENDING);
        if (pendings.isEmpty()) {
            List<PlanParticipant> participants = participantRepository.findParticipantsByPlanId(plan.getId());
            List<Notification> notifications = participants.stream()
                    .map(p -> Notification.builder()
                            .planId(plan.getId())
                            .meetId(plan.getMeetId())
                            .action(Action.PENDING)
                            .userId(p.getUserId())
                            .scheduledAt(event.getPlanTime())
                            .build()
                    ).toList();

            notificationRepository.saveAll(notifications);
        }

        LocalDateTime planTime = plan.getPlanTime().atZone(KST).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime requestTime = event.getPlanTime().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime().truncatedTo(ChronoUnit.SECONDS);

        if (planTime.isBefore(requestTime)) {
            return;
        }

        if (!planTime.equals(requestTime)) {
            long hours = Math.min(2, Math.max(1, ChronoUnit.HOURS.between(LocalDateTime.now(KST), planTime)));
            LocalDateTime runAt = planTime.minusHours(hours).atZone(KST).toLocalDateTime();

            PlanRemindEvent remindEvent = PlanRemindEvent.builder()
                    .meetId(plan.getMeetId())
                    .meetName(event.getMeetName())
                    .planId(plan.getId())
                    .planName(plan.getName())
                    .planTime(plan.getPlanTime())
                    .planCreatorId(event.getPlanCreatorId())
                    .build();

            outboxService.saveWithRunAt(PLAN_REMIND, PLAN, plan.getId(), runAt, remindEvent);
            return;
        }

        WeatherInfoScheduleResponse weather =
                new WeatherInfoScheduleResponse(weatherInfo(plan.getLongitude(), plan.getLatitude()));

        PlanRemindNotifyEvent notifyEvent = PlanRemindNotifyEvent.builder()
                .meetId(plan.getMeetId())
                .meetName(event.getMeetName())
                .planId(plan.getId())
                .planName(plan.getName())
                .planTime(plan.getPlanTime())
                .planCreatorId(event.getPlanCreatorId())
                .temperature(weather.temperature())
                .iconImage(weather.weatherIconImage())
                .build();

        sendService.sendMultiNotification(notifyEvent);
    }

    private OpenWeatherResponse weatherInfo(BigDecimal lot, BigDecimal lat) {
        return weatherService.getWeatherInfoByLocation(new CoordinateRequest(lot, lat)).join();
    }
}
