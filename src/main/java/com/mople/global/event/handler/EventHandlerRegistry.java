package com.mople.global.event.handler;

import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.global.enums.ExceptionReturnCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EventHandlerRegistry<H, E> {

    private final Map<Class<? extends E>, H> handlerMap = new HashMap<>();
    private final List<H> handlers;
    private final Function<H, Class<? extends E>> keyExtractor;

    public EventHandlerRegistry(List<H> handlers, Function<H, Class<? extends E>> keyExtractor) {
        this.handlers = handlers;
        this.keyExtractor = keyExtractor;

        for (H handler : handlers) {
            Class<? extends E> key = keyExtractor.apply(handler);
            if (handlerMap.putIfAbsent(key, handler) != null) {
                throw new IllegalStatesException(ExceptionReturnCode.ILLEGAL_HANDLER_TYPE);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public H getHandler(E event) {
        H handler = handlerMap.get(event.getClass());

        if (handler != null) {
            return handler;
        }

        for (H h : handlers) {
            if (keyExtractor.apply(h).isAssignableFrom(event.getClass())) {
                return h;
            }
        }
        throw new IllegalStatesException(ExceptionReturnCode.ILLEGAL_HANDLER_TYPE);
    }
}
