package com.groupMeeting.core.config;

import com.groupMeeting.core.interceptor.version.ForceUpdateInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ForceUpdateConfig implements WebMvcConfigurer {
    private final ForceUpdateInterceptor forceUpdateInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(forceUpdateInterceptor)
                .order(1)
                .addPathPatterns("/**")
                // image
                .excludePathPatterns("/error", "/favicon.ico")
                // swagger
                .excludePathPatterns("/docs", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
                // user registry
                .excludePathPatterns("/auth/**", "/user/nickname/**", "/image/upload/**")
                // admin path
                .excludePathPatterns("/v1/**")
                // invite path
                .excludePathPatterns("/invite/**","/user/removeTest");
    }
}
