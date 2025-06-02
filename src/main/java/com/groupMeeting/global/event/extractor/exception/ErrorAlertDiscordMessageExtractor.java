package com.groupMeeting.global.event.extractor.exception;

import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.data.exception.ErrorAlertMessage;

public class ErrorAlertDiscordMessageExtractor implements DiscordMessageExtractor<ErrorAlertMessage> {
    @Override
    public DiscordMessage extract(ErrorAlertMessage message) {
        return null;
    }
}
