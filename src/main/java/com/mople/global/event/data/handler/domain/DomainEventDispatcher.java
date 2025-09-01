package com.mople.global.event.data.handler.domain;

import com.mople.dto.event.data.domain.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DomainEventDispatcher {

    private final Map<Class<?>, DomainEventHandler<?>> handlers = new HashMap<>();

    public DomainEventDispatcher(List<DomainEventHandler<?>> list) {
        for (var h : list) {
            handlers.put(h.supports(), h);
        }
    }

    @SuppressWarnings("unchecked")
    public void dispatch(DomainEvent event) {
        var h = handlers.get(event.getClass());

        if (h == null) {
            throw new IllegalStateException("No handler for " + event.getClass());
        }

        ((DomainEventHandler<DomainEvent>) h).handle(event);
    }
}
