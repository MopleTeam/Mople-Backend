package com.mople.global.event.listener.exception;

import com.mople.core.annotation.event.ApplicationEventListener;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.ErrorAlertMessage;
import com.mople.global.event.extractor.MessageExtractor;
import com.mople.global.event.sender.MessageSender;
import com.mople.global.event.template.MessageTemplate;

import org.springframework.beans.factory.annotation.Qualifier;

@ApplicationEventListener
public class ErrorAlertEventListener {
    private final MessageTemplate messageTemplate;
    private final MessageExtractor<ErrorAlertMessage, DiscordMessage> messageExtractor;
    private final MessageSender<DiscordMessage> messageSender;

    public ErrorAlertEventListener(
            MessageTemplate messageTemplate,
            @Qualifier("errorAlertMessageExtractor") MessageExtractor<ErrorAlertMessage, DiscordMessage> messageExtractor,
            @Qualifier("discordMessageSender") MessageSender<DiscordMessage> messageSender) {
        this.messageTemplate = messageTemplate;
        this.messageExtractor = messageExtractor;
        this.messageSender = messageSender;
    }
}
