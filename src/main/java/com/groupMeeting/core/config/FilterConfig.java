package com.groupMeeting.core.config;

import com.groupMeeting.core.filter.MdcLoggingFilter;
import com.groupMeeting.global.logging.LoggingContextManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {
    private final LoggingContextManager loggingContextManager;

    @Bean
    public FilterRegistrationBean<MdcLoggingFilter> mdcLoggingFilterRegistration() {
        FilterRegistrationBean<MdcLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MdcLoggingFilter(loggingContextManager));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
