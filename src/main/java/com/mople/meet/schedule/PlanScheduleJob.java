package com.mople.meet.schedule;

import com.mople.core.exception.custom.ResourceNotFoundException;
import com.mople.dto.event.data.plan.PlanRemindEventData;
import com.mople.dto.event.data.review.ReviewRemindEventData;
import com.mople.dto.request.weather.CoordinateRequest;
import com.mople.dto.response.weather.OpenWeatherResponse;
import com.mople.dto.response.weather.WeatherInfoScheduleResponse;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.notification.Notification;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.event.data.notify.NotifyEventPublisher;
import com.mople.global.event.data.notify.RescheduleNotifyPublisher;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;
import com.mople.notification.repository.NotificationRepository;
import com.mople.weather.service.WeatherService;

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

import static com.mople.global.enums.Action.PENDING;

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
                        PlanRemindEventData.builder()
                                .meetId(meetPlan.getMeet().getId())
                                .meetName(meetPlan.getMeet().getName())
                                .planId(meetPlan.getId())
                                .planName(meetPlan.getName())
                                .planTime(meetPlan.getPlanTime())
                                .creatorId(userId)
                                .temperature(weather.temperature())
                                .iconImage(weather.weatherIconImage())
                                .build()
                )
        );
    }

    public void remindReschedule(Long planId, LocalDateTime planTime, Long userId) {
        publisher.publishEvent(new RescheduleNotifyPublisher(planId, planTime, userId));
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
                            ReviewRemindEventData.builder()
                                    .meetId(review.getMeet().getId())
                                    .meetName(review.getMeet().getName())
                                    .reviewId(review.getId())
                                    .reviewName(review.getName())
                                    .creatorId(review.getCreatorId())
                                    .build()
                    )
            );
        }
    }
}
