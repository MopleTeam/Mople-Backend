package com.mople.global.event.handler.domain.impl.user;

import com.mople.dto.event.data.domain.meet.MeetLeftEvent;
import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.dto.event.data.domain.user.UserDeletedEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.MeetRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.Status.DELETED;
import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.*;
import static com.mople.global.utils.batch.Batching.chunk;

@Component
@RequiredArgsConstructor
public class UserDeletedFanoutHandler implements DomainEventHandler<UserDeletedEvent> {

    private final MeetRepository meetRepository;
    private final MeetMemberRepository memberRepository;
    private final OutboxService outboxService;

    @Override
    public Class<UserDeletedEvent> getHandledType() {
        return UserDeletedEvent.class;
    }

    @Override
    public void handle(UserDeletedEvent event) {
        List<Long> joinedMeetIds = memberRepository.findMeetIdsByUserId(event.getUserId());

        List<Long> ownedMeetIds = meetRepository.findIdsByCreatorId(event.getUserId());
        List<Long> memberMeetIds = joinedMeetIds.stream()
                .filter(id -> !ownedMeetIds.contains(id))
                .toList();


        chunk(ownedMeetIds, ids -> {
            meetRepository.softDeleteAll(DELETED, ids, event.getUserId());

            ids.forEach(id -> {
                MeetSoftDeletedEvent deleteEvent = MeetSoftDeletedEvent.builder()
                        .meetId(id)
                        .meetDeletedBy(event.getUserId())
                        .build();

                outboxService.save(MEET_SOFT_DELETED, MEET, id, deleteEvent);
            });
        });

        chunk(memberMeetIds, ids ->
            ids.forEach(id -> {
                MeetLeftEvent deleteEvent = MeetLeftEvent.builder()
                        .meetId(id)
                        .leaveMemberId(event.getUserId())
                        .build();

                outboxService.save(MEET_LEFT, MEET, id, deleteEvent);
            })
        );
    }
}
