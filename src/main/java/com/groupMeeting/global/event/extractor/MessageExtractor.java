package com.groupMeeting.global.event.extractor;

@FunctionalInterface
public interface MessageExtractor<T, U> {
    U extract(T message);
}
