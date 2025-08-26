package com.mople.global.event.listener.domain.plan;

import com.mople.dto.event.data.domain.plan.PlanDeleteEvent;
import com.mople.meet.repository.MeetTimeRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PlanDeleteCleanupListener {

    private final MeetTimeRepository timeRepository;
    private final PlanParticipantRepository participantRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void pushEventListener(PlanDeleteEvent event) {
        participantRepository.deleteByPlanId(event.planId());
        timeRepository.deleteByPlanId(event.planId());
    }
}
