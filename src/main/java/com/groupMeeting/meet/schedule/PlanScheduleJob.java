package com.groupMeeting.meet.schedule;

import com.groupMeeting.core.exception.custom.ResourceNotFoundException;
import com.groupMeeting.dto.request.weather.CoordinateRequest;
import com.groupMeeting.dto.response.weather.OpenWeatherResponse;
import com.groupMeeting.dto.response.weather.WeatherInfoScheduleResponse;
import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.review.PlanReview;
import com.groupMeeting.entity.notification.Notification;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.event.data.notify.NotifyEventPublisher;
import com.groupMeeting.global.event.data.notify.rescheduleNotifyPublisher;
import com.groupMeeting.meet.repository.plan.MeetPlanRepository;
import com.groupMeeting.meet.repository.review.PlanReviewRepository;
import com.groupMeeting.notification.repository.NotificationRepository;
import com.groupMeeting.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static com.groupMeeting.global.enums.Action.PENDING;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanScheduleJob {
    private final WeatherService weatherService;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReviewRepository reviewRepository;
    private final ApplicationEventPublisher publisher;
    private final NotificationRepository notificationRepository;

    @Async
    @Transactional
    public void planRemindSchedule(Long planId, LocalDateTime time, Long userId) {
        log.info("planRemindSchedule planId = {}, time = {}, userId = {}", planId, time, userId);

        MeetPlan meetPlan = meetPlanRepository.findPlanAndMeet(planId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_PLAN)
                );

        List<Notification> planRemindNotification =
                notificationRepository.findPlanRemindNotification(planId, PENDING);

        if (planRemindNotification.isEmpty()) {
            List<Notification> notifications = meetPlan
                    .getParticipants()
                    .stream()
                    .map(p ->
                            Notification.builder()
                                    .planId(planId)
                                    .meetId(meetPlan.getMeet().getId())
                                    .action(PENDING)
                                    .user(p.getUser())
                                    .scheduledAt(time)
                                    .build()
                    )
                    .toList();

            notificationRepository.saveAll(notifications);
        }

        LocalDateTime planTime = meetPlan.getPlanTime().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime requestTime = time.truncatedTo(ChronoUnit.SECONDS);

        if (planTime.isBefore(requestTime)) {
            log.info("is Before planTime = {}, time = {}", planTime, requestTime);
            return;
        }

        if (!planTime.equals(requestTime)) {
            log.info("reSchedule planTime = {}, time = {}", planTime, requestTime);
            remindReschedule(meetPlan.getId(), meetPlan.getPlanTime(), userId);
            return;
        }

        WeatherInfoScheduleResponse weather =
                new WeatherInfoScheduleResponse(weatherInfo(meetPlan.getLongitude(), meetPlan.getLatitude()));

        publisher.publishEvent(
                NotifyEventPublisher.planRemind(
                        Map.of(
                                "planId", meetPlan.getId().toString(),
                                "meetId", meetPlan.getMeet().getId().toString(),
                                "meetName", meetPlan.getMeet().getName(),
                                "planName", meetPlan.getName(),
                                "planTime", meetPlan.getPlanTime().toString(),
                                "userId", userId.toString(),
                                "temperature", weather.temperature().toString(),
                                "iconImage", weather.weatherIconImage()
                        ),
                        Map.of(
                                "planId", meetPlan.getId().toString()
//                                "meetId", meetPlan.getMeet().getId().toString()
                        )
                )
        );
    }

    public void remindReschedule(Long planId, LocalDateTime planTime, Long userId) {
        publisher.publishEvent(new rescheduleNotifyPublisher(planId, planTime, userId));
    }

    public OpenWeatherResponse weatherInfo(BigDecimal lot, BigDecimal lat) {
        return weatherService.getWeatherInfoByLocation(new CoordinateRequest(lot, lat)).join();
    }

    @Async
    @Transactional
    public void reviewRemindJob(Long reviewId) {
        PlanReview review = reviewRepository.findReview(reviewId).orElseThrow(
                () -> new ResourceNotFoundException(ExceptionReturnCode.NOT_FOUND_REVIEW)
        );

        if (!review.getUpload()) {

            publisher.publishEvent(
                    NotifyEventPublisher.reviewRemind(
                            Map.of(
                                    "reviewId", review.getId().toString(),
                                    "creatorId", review.getCreatorId().toString(),
                                    "meetId", review.getMeet().getId().toString(),
                                    "meetName", review.getMeet().getName(),
                                    "reviewName", review.getName()
                            ),
                            Map.of(
                                    "reviewId", review.getId().toString()
//                                    "meetId", review.getMeet().getId().toString()
                            )
                    )
            );
        }
    }
}
