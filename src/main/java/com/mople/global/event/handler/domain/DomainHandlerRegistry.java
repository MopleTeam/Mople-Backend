package com.mople.global.event.handler.domain;

import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.event.handler.EventHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DomainHandlerRegistry
        extends EventHandlerRegistry<DomainEventHandler<? extends DomainEvent>, DomainEvent> {

    @Autowired
    public DomainHandlerRegistry(List<DomainEventHandler<? extends DomainEvent>> handlers) {
        super(handlers, DomainEventHandler::getHandledType);
    }

    @Override
    public List<DomainEventHandler<? extends DomainEvent>> getHandler(DomainEvent event) {
        return super.getHandler(event);
    }
}
