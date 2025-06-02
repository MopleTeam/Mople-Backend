package com.groupMeeting.global.async.message;

import com.groupMeeting.global.client.DiscordMessageClient;
import com.groupMeeting.global.event.data.exception.DiscordMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordExceptionSender {
    private final DiscordMessageClient client;

    @Async
    public void exceptionSend(DiscordMessage message) {
        CompletableFuture.runAsync(() -> {
            client.sendMessage(message);
        });
    }
}