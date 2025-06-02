package com.groupMeeting.core.config;

import jakarta.annotation.PostConstruct;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.TimeZone;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EntityScan("com.groupMeeting.entity.**")
@EnableJpaRepositories("com.groupMeeting.*.repository")
public class JpaAuditingConfig {

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
