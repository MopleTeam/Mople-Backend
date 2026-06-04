package com.mople.dto.event.data.domain.notice;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record NoticePurgeEvent(
        Long noticeId
) implements DomainEvent {
}
