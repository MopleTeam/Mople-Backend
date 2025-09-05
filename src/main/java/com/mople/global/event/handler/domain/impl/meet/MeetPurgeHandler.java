package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.meet.MeetPurgeEvent;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MeetPurgeHandler implements DomainEventHandler<MeetPurgeEvent> {

    private final MeetRepository meetRepository;

    @Override
    public Class<MeetPurgeEvent> getHandledType() {
        return MeetPurgeEvent.class;
    }

    @Override
    public void handle(MeetPurgeEvent event) {
        Status meetStatus = meetRepository.findStatusById(event.meetId());

        if (!Objects.equals(meetStatus, Status.DELETED)) {
            return;
        }

        meetRepository.deleteById(event.meetId());
    }
}
