package com.groupMeeting.core.config.component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@ConfigurationProperties(prefix = "cors")
@RequiredArgsConstructor
public class CorsConfig {
    private final List<String> allowOrigins;
    private final List<String> allowMethods;
    private final List<String> allowHeaders;
}
