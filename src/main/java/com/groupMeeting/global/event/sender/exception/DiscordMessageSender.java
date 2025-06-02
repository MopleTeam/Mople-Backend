package com.groupMeeting.global.event.sender.exception;

import com.groupMeeting.global.client.DiscordMessageClient;
import com.groupMeeting.global.event.data.exception.DiscordMessage;
import com.groupMeeting.global.event.sender.MessageSender;

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
