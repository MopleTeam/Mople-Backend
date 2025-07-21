package com.mople.global.event.extractor.exception;

import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.ErrorAlertMessage;

public class ErrorAlertDiscordMessageExtractor implements DiscordMessageExtractor<ErrorAlertMessage> {
    @Override
    public DiscordMessage extract(ErrorAlertMessage message) {
        return null;
    }
}
