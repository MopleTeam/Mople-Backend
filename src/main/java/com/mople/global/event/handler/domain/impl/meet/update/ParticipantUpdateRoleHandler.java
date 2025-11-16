package com.mople.global.event.handler.domain.impl.meet.update;

import com.mople.dto.event.data.domain.meet.HostChangedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.plan.PlanParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantUpdateRoleHandler implements DomainEventHandler<HostChangedEvent> {

    private final PlanParticipantRepository participantRepository;

    @Override
    public Class<HostChangedEvent> getHandledType() {
        return HostChangedEvent.class;
    }

    @Override
    public void handle(HostChangedEvent event) {
        participantRepository.updateOldHostRoles(event.meetId(), event.oldHostId());
        participantRepository.updateNewHostRoles(event.meetId(), event.newHostId());
    }
}
