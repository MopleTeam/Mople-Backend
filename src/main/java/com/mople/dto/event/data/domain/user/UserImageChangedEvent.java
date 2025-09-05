package com.mople.dto.event.data.domain.user;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mople.dto.event.data.domain.DomainEvent;
import lombok.Builder;
import lombok.Getter;

import static com.mople.global.enums.event.EventTypeNames.USER_IMAGE_CHANGED;

@JsonTypeName(USER_IMAGE_CHANGED)
@Builder
@Getter
public class UserImageChangedEvent implements DomainEvent {

    private final Long userId;
    private final String imageUrl;
    private final Long imageDeletedBy;
}
