package com.groupMeeting.global.event.listener.exception;

import com.groupMeeting.core.annotation.event.ApplicationEventListener;
import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.data.exception.ErrorAlertMessage;
import com.groupMeeting.global.event.extractor.MessageExtractor;
import com.groupMeeting.global.event.sender.MessageSender;
import com.groupMeeting.global.event.template.MessageTemplate;

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
