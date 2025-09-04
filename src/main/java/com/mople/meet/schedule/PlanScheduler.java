package com.mople.meet.schedule;

import com.mople.entity.meet.plan.MeetPlan;
import com.mople.global.enums.Status;
import com.mople.meet.repository.plan.MeetPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanScheduler {

    private final PlanTransitionService transitionService;
    private final MeetPlanRepository meetPlanRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void previousPlanReviewChangeSchedule() {
        List<MeetPlan> previousPlanAll = meetPlanRepository.findPreviousPlanAll(LocalDateTime.now(), Status.ACTIVE);

        for (MeetPlan plan : previousPlanAll) {
            transitionService.transitionPlanByOne(plan.getId());
        }
    }
}