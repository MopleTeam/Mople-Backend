package com.mople.core.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableFeignClients(basePackages = "com.mople.global.client")
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(Duration.ofSeconds(10), Duration.ofSeconds(30), false);
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(100L, 2L, 1);
    }

}
