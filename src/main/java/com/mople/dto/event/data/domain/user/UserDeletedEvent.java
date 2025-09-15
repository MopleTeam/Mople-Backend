package com.mople.dto.event.data.domain.user;

import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;

@Builder
public record UserDeletedEvent(
        Long userId,
        String userProfileImg
) implements DomainEvent {
}
