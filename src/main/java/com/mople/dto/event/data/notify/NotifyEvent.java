package com.mople.dto.event.data.notify;

import java.util.Map;

public interface NotifyEvent {

    String getTitle();

    String getBody();

    Map<String, String> getRoutingKey();
}
