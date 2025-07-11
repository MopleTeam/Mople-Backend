package com.groupMeeting.global.event.template;

import com.groupMeeting.global.event.extractor.MessageExtractor;
import com.groupMeeting.global.event.sender.MessageSender;

public class MessageTemplate {
    <T, U> void sendMessage(
            T message,
            MessageExtractor<T, U> extractor,
            MessageSender<U> sender
    ) {
        U extractMessage = extractor.extract(message);
        sender.send(extractMessage);
    }
}