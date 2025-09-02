package com.mople.global.event.handler.domain.impl.meet;

import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetCleanupHandler implements DomainEventHandler<MeetSoftDeletedEvent> {

    private final MeetMemberRepository memberRepository;

    @Override
    public Class<MeetSoftDeletedEvent> getHandledType() {
        return MeetSoftDeletedEvent.class;
    }

    @Override
    public void handle(MeetSoftDeletedEvent event) {
        memberRepository.deleteByMeetId(event.getMeetId());
    }
}
