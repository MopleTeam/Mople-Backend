package com.mople.core.mapping;

import com.mople.entity.policy.ApiVersionPolicy;
import com.mople.global.enums.Os;
import com.mople.policy.service.ApiVersionPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

@RequiredArgsConstructor
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {
    private final ApiVersionPolicyService apiVersionPolicyService;
    private final String requiredVersion;

    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        return this;
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        Os os = Os.from(request.getHeader("os"));
        String appVersion = request.getHeader("version");
        String uri = request.getRequestURI();

        ApiVersionPolicy apiVersionPolicy = apiVersionPolicyService.getApiVersionPolicy(os, uri, appVersion);
        return apiVersionPolicy != null && requiredVersion.equals(apiVersionPolicy.getApiVersion()) ? this : null;
    }

    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        return 0;
    }
}
