package com.mople.global.event.handler.domain.impl.meet;

import com.mople.core.exception.custom.NonRetryableOutboxException;
import com.mople.dto.event.data.domain.image.ImageDeletedEvent;
import com.mople.dto.event.data.domain.meet.MeetSoftDeletedEvent;
import com.mople.entity.meet.Meet;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.global.enums.Status;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.MeetInviteRepository;
import com.mople.meet.repository.MeetMemberRepository;
import com.mople.meet.repository.MeetRepository;
import com.mople.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mople.global.enums.event.AggregateType.MEET;
import static com.mople.global.enums.event.EventTypeNames.IMAGE_DELETED;

@Component
@RequiredArgsConstructor
public class MeetCleanupHandler implements DomainEventHandler<MeetSoftDeletedEvent> {

    private final MeetRepository meetRepository;
    private final MeetMemberRepository memberRepository;
    private final MeetInviteRepository inviteRepository;
    private final OutboxService outboxService;

    @Override
    public Class<MeetSoftDeletedEvent> getHandledType() {
        return MeetSoftDeletedEvent.class;
    }

    @Override
    public void handle(MeetSoftDeletedEvent event) {
        memberRepository.deleteByMeetId(event.meetId());
        inviteRepository.deleteByMeetId(event.meetId());

        Meet meet = meetRepository.findByIdAndStatus(event.meetId(), Status.DELETED)
                .orElseThrow(() -> new NonRetryableOutboxException(ExceptionReturnCode.NOT_FOUND_MEET));

        if (meet.getMeetImage() == null || meet.getMeetImage().isBlank()) {
            return;
        }

        ImageDeletedEvent deletedEvent = ImageDeletedEvent.builder()
                .aggregateType(MEET)
                .aggregateId(event.meetId())
                .imageUrl(meet.getMeetImage())
                .imageDeletedBy(event.meetDeletedBy())
                .build();

        outboxService.save(IMAGE_DELETED, MEET, event.meetId(), deletedEvent);
    }
}
