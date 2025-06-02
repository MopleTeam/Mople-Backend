package com.groupMeeting.core.config;

import com.groupMeeting.core.config.component.CorsConfig;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CorsConfig.class})
public class ComponentScanConfig {
}
