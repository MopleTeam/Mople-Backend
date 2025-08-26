package com.mople.meet.schedule;

import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;
import com.mople.meet.repository.MeetRepository;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import com.mople.meet.repository.review.PlanReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class PlanScheduler {

    private final MeetRepository meetRepository;
    private final MeetPlanRepository meetPlanRepository;
    private final PlanReviewRepository planReviewRepository;
    private final PlanParticipantRepository participantRepository;

    private final ReviewReminderScheduleJob scheduleJob;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void previousPlanReviewChangeSchedule() {
        List<MeetPlan> previousPlanAll = meetPlanRepository.findPreviousPlanAll(LocalDateTime.now());

        List<PlanReview> reviews = planReviewRepository.saveAll(
                previousPlanAll
                        .stream()
                        .map(p -> PlanReview
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
                                .creatorId(p.getCreatorId())
                                .meetId(p.getMeetId())
                                .build()
                        ).toList()
        );

        Map<Long, Long> planToReviewId = reviews.stream()
                .collect(toMap(PlanReview::getPlanId, PlanReview::getId));

        Map<Long, String> meetNames = reviews.stream()
                .map(PlanReview::getMeetId)
                .distinct()
                .collect(toMap(id -> id,
                        id -> meetRepository.findById(id).orElseThrow().getName()));

        for (MeetPlan p : previousPlanAll) {
            Long reviewId = planToReviewId.get(p.getId());

            List<PlanParticipant> participants = participantRepository.findParticipantsByPlanId(p.getId());
            participants.forEach(pp -> pp.updateReview(reviewId));
            participantRepository.saveAll(participants);
        }

        meetPlanRepository.deleteAll(previousPlanAll);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override public void afterCommit() {
                        reviews.forEach(r ->
                                scheduleJob.scheduleReminder(r.getId(), meetNames.get(r.getMeetId()))
                        );
                    }
                }
        );
    }
}