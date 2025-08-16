package com.mople.core.config;

import com.mople.core.mapping.ApiVersionHandlerMapping;
import com.mople.policy.service.ApiVersionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Configuration
@RequiredArgsConstructor
public class ApiVersionConfig implements WebMvcRegistrations {
    private final ApiVersionPolicyService apiVersionPolicyService;

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new ApiVersionHandlerMapping(apiVersionPolicyService);
    }
}
