package com.mople.global.event.handler.domain.impl.meet.leave;

import com.mople.dto.event.data.domain.meet.MeetLeftEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.MeetPlanRepository;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlanParticipantDeleteHandler implements DomainEventHandler<MeetLeftEvent> {

    private final MeetPlanRepository planRepository;
    private final PlanParticipantRepository participantRepository;

    @Override
    public Class<MeetLeftEvent> getHandledType() {
        return MeetLeftEvent.class;
    }

    @Override
    public void handle(MeetLeftEvent event) {
        List<Long> planIds = planRepository.findIdsByMeetId(event.meetId());
        participantRepository.deleteByPlanIdsAndUserId(planIds, event.leaveMemberId());
    }
}
