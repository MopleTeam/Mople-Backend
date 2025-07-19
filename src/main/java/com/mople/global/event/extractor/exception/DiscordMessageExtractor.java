package com.mople.global.event.extractor.exception;

import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.extractor.MessageExtractor;

@FunctionalInterface
public interface DiscordMessageExtractor<T> extends MessageExtractor<T, DiscordMessage> {
    @Override
    DiscordMessage extract(T message);
}