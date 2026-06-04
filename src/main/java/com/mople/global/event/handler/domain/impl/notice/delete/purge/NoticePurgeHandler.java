package com.mople.global.event.handler.domain.impl.notice.delete.purge;

import com.mople.dto.event.data.domain.notice.NoticePurgeEvent;
import com.mople.global.event.handler.domain.DomainEventHandler;
import com.mople.meet.repository.notice.MeetNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoticePurgeHandler implements DomainEventHandler<NoticePurgeEvent> {

    private final MeetNoticeRepository noticeRepository;

    @Override
    public Class<NoticePurgeEvent> getHandledType() {
        return NoticePurgeEvent.class;
    }

    @Override
    public void handle(NoticePurgeEvent event) {
        noticeRepository.hardDeleteById(event.noticeId());
    }
}
