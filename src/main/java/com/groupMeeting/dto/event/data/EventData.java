package com.groupMeeting.dto.event.data;

import java.util.Map;

public interface EventData {

    String getTitle();

    String getBody();

    Map<String, String> getRoutingKey();
}
