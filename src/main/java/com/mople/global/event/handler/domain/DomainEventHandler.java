package com.mople.global.event.handler.domain;

import com.mople.dto.event.data.domain.DomainEvent;

public interface DomainEventHandler<T extends DomainEvent> {

    Class<T> supports();

    void handle(T event);
}
