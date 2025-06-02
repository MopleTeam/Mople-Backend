package com.groupMeeting.global.event.extractor.exception;

import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.extractor.MessageExtractor;

@FunctionalInterface
public interface DiscordMessageExtractor<T> extends MessageExtractor<T, DiscordMessage> {
    @Override
    DiscordMessage extract(T message);
}