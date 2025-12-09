package com.mople.core.config.component;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "cache.caffeine")
public record  CacheSpecsConfig(
        Map<String, String> specs
){
}
