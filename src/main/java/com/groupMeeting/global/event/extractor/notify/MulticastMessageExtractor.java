package com.groupMeeting.global.event.extractor.notify;

import com.google.firebase.messaging.MulticastMessage;

import com.groupMeeting.global.event.extractor.MessageExtractor;

@FunctionalInterface
public interface MulticastMessageExtractor<T> extends MessageExtractor<T, MulticastMessage> {
    @Override
    MulticastMessage extract(T message);
}
