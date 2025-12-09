package com.mople.global.event.handler.domain;

import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.dto.event.data.domain.DomainEvent;
import com.mople.global.enums.ExceptionReturnCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DomainHandlerRegistry {

    private final Map<Class<?>, List<DomainEventHandler<? extends DomainEvent>>> handlerMap;

    @Autowired
    public DomainHandlerRegistry(List<DomainEventHandler<? extends DomainEvent>> handlers) {
        Map<Class<?>, List<DomainEventHandler<? extends DomainEvent>>> grouped = new HashMap<>();
        for (DomainEventHandler<? extends DomainEvent> h : handlers) {
            grouped.computeIfAbsent(h.getHandledType(), k -> new ArrayList<>()).add(h);
        }

        grouped.replaceAll((k, list) -> {
            list.sort(
                    Comparator
                            .comparingInt((DomainEventHandler<?> h) -> h.getOrder())
                            .thenComparing(h -> h.getClass().getName())
            );
            return List.copyOf(list);
        });

        this.handlerMap = Map.copyOf(grouped);
    }

    @SuppressWarnings("unchecked")
    public List<DomainEventHandler<? extends DomainEvent>> getHandler(DomainEvent event) {
        List<DomainEventHandler<? extends DomainEvent>> handlers = handlerMap.get(event.getClass());

        if (handlers == null || handlers.isEmpty()) {
            throw new IllegalStatesException(ExceptionReturnCode.ILLEGAL_HANDLER_TYPE);
        }
        return handlers;
    }
}
