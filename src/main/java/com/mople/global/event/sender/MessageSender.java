package com.mople.global.event.sender;

@FunctionalInterface
public interface MessageSender<T> {
    void send(T message);
}
