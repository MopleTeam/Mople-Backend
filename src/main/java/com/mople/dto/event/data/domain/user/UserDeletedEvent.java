package com.mople.dto.event.data.domain.user;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.USER_DELETED;

@JsonTypeName(USER_DELETED)
@Builder
@Getter
public class UserDeletedEvent implements DomainEvent {

    private final Long userId;
}
