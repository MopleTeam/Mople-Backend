package com.mople.global.event.extractor;

@FunctionalInterface
public interface MessageExtractor<T, U> {
    U extract(T message);
}
