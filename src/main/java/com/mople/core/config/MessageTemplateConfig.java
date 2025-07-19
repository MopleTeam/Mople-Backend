package com.mople.core.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mople.global.client.DiscordMessageClient;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.data.exception.ErrorAlertMessage;
import com.mople.global.event.extractor.exception.ErrorAlertDiscordMessageExtractor;
import com.mople.global.event.extractor.MessageExtractor;
import com.mople.global.event.sender.exception.DiscordMessageSender;
import com.mople.global.event.sender.MessageSender;
import com.mople.global.event.template.MessageTemplate;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MessageTemplateConfig {
    private final DiscordMessageClient client;
    private final FirebaseMessaging messaging;

    @Bean
    public MessageTemplate messageTemplate() {
        return new MessageTemplate();
    }

    @Bean
    public MessageExtractor<ErrorAlertMessage, DiscordMessage> errorAlertMessageExtractor() {
        return new ErrorAlertDiscordMessageExtractor();
    }

    @Bean
    public MessageSender<DiscordMessage> discordMessageSender() {
        return new DiscordMessageSender(client);
    }

//    @Bean
//    public MessageExtractor<ErrorAlertMessage, DiscordMessage> errorAlertMessageExtractor() {
//        return new ErrorAlertDiscordMessageExtractor();
//    }
//
//    @Bean
//    public MessageSender<DiscordMessage> discordMessageSender() {
//        return new DiscordMessageSender(client);
//    }
}
