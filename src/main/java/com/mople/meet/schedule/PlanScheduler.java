package com.mople.meet.schedule;

import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.review.PlanReviewRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanScheduler {
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReviewRepository planReviewRepository;
    private final TaskScheduler taskScheduler;
    private final PlanScheduleJob planScheduleJob;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void previousPlanReviewChangeSchedule() {
        List<MeetPlan> previousPlanAll = meetPlanRepository.findPreviousPlanAll(LocalDateTime.now());

        List<PlanReview> planReviews = planReviewRepository.saveAll(
                previousPlanAll
                        .stream()
                        .map(p -> {
                            PlanReview review = PlanReview
                                    .builder()
                                    .planId(p.getId())
                                    .name(p.getName())
                                    .planTime(p.getPlanTime())
                                    .address(p.getAddress())
                                    .title(p.getTitle())
                                    .latitude(p.getLatitude())
                                    .longitude(p.getLongitude())
                                    .weatherIcon(p.getWeatherIcon())
                                    .weatherAddress(p.getWeatherAddress())
                                    .temperature(p.getTemperature())
                                    .pop(p.getPop())
                                    .creatorId(p.getCreator().getId())
                                    .build();

                            p.getMeet().addReview(review);
                            p.getMeet().removePlan(p);

                            p.getParticipants().forEach(participant -> participant.updateReview(review));

                            review.updateParticipants(p.getParticipants());

                            return review;
                        })
                        .toList()
        );

        meetPlanRepository.deleteAll(previousPlanAll);

        planReviews.forEach(review -> {
            taskScheduler.schedule(
                    () -> planScheduleJob.reviewRemindJob(review.getId()),
                    LocalDateTime
                            .of(LocalDate.now(), LocalTime.of(12, 0, 0))
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            );
        });
    }
}