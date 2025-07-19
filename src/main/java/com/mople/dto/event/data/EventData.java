package com.mople.dto.event.data;

import java.util.Map;

public interface EventData {

    String getTitle();

    String getBody();

    Map<String, String> getRoutingKey();
}
