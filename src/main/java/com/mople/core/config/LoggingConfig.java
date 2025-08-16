package com.mople.core.config;

import com.mople.core.interceptor.LoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class LoggingConfig implements WebMvcConfigurer {
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                // swagger
                .excludePathPatterns("/docs", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
                // 에러 발생 시 내부적으로 /error 포워딩 되는 것은 Internal Api 로거로 기록하지 않기 위해
                .excludePathPatterns("/error")
                // user registry
                .excludePathPatterns("/auth/**", "/user/nickname/**", "/image/upload/**")
                // admin path
                .excludePathPatterns("/v1/**")
                // invite path
                .excludePathPatterns("/invite/**","/user/removeTest")
                // policy path
                .excludePathPatterns("/policy/**");
    }
}
