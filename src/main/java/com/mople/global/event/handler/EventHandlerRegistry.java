package com.mople.global.event.handler;

import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.global.enums.ExceptionReturnCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EventHandlerRegistry<H, E> {

    private final Map<Class<? extends E>, List<H>> handlerMap = new HashMap<>();
    private final List<H> handlers;
    private final Function<H, Class<? extends E>> keyExtractor;

    public EventHandlerRegistry(List<H> handlers, Function<H, Class<? extends E>> keyExtractor) {
        this.handlers = handlers;
        this.keyExtractor = keyExtractor;

        for (H handler : handlers) {
            Class<? extends E> key = keyExtractor.apply(handler);
            handlerMap
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(handler);
        }
    }

    @SuppressWarnings("unchecked")
    public List<H> getHandler(E event) {
        List<H> matched = new ArrayList<>();

        List<H> direct = handlerMap.get(event.getClass());
        if (direct != null) {
            matched.addAll(direct);
        }

        for (H h : handlers) {
            if (keyExtractor.apply(h).isAssignableFrom(event.getClass()) &&
                    !matched.contains(h)) {
                matched.add(h);
            }
        }

        if (matched.isEmpty()) {
            throw new IllegalStatesException(ExceptionReturnCode.ILLEGAL_HANDLER_TYPE);
        }
        return matched;
    }
}
