package com.mople.global.event.sender.exception;

import com.mople.global.client.DiscordMessageClient;
import com.mople.global.event.data.exception.DiscordMessage;
import com.mople.global.event.sender.MessageSender;

public class DiscordMessageSender implements MessageSender<DiscordMessage> {
    private final DiscordMessageClient client;

    public DiscordMessageSender(DiscordMessageClient client) {
        this.client = client;
    }

    @Override
    public void send(DiscordMessage message) {
        client.sendMessage(message);
    }
}
