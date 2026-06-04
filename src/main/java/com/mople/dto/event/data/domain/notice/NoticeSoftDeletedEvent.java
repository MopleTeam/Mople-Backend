package com.mople.dto.event.data.domain.notice;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record NoticeSoftDeletedEvent(
        Long noticeId,
        Long noticeDeletedBy
) implements DomainEvent {
}
