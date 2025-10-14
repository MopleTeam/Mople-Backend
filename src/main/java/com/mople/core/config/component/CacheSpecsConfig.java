package com.mople.core.config.component;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.caffeine.spec")
public record  CacheSpecsConfig(
        String homeViewPlan
) {
}