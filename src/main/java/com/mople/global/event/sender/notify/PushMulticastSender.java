package com.mople.global.event.sender.notify;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;

import com.mople.global.event.sender.MessageSender;

public class PushMulticastSender implements MessageSender<MulticastMessage> {
    private final FirebaseMessaging sender;

    public PushMulticastSender(FirebaseMessaging message) {
        this.sender = message;
    }

    @Override
    public void send(MulticastMessage message) {
        sender.sendEachForMulticastAsync(message);
    }
}
