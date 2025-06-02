package com.groupMeeting.global.event.extractor.notify;

import com.google.firebase.messaging.MulticastMessage;

public class PushMulticastMessageExtractor implements MulticastMessageExtractor<Object> {
    @Override
    public MulticastMessage extract(Object message) {
        return null;
    }
}
