package com.mople.core.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.mople.core.config.component.CacheSpecsConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheSpecsConfig.class)
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(CacheSpecsConfig properties) {
        CaffeineCache homeViewPlan = new CaffeineCache(
                "homeViewPlan",
                Caffeine.from(properties.homeViewPlan()).recordStats().build()
        );

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(homeViewPlan));
        return manager;
    }
}
