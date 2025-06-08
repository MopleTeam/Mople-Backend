package com.groupMeeting.core.mapping;

import com.groupMeeting.entity.policy.ApiVersionPolicy;
import com.groupMeeting.policy.service.ApiVersionPolicyService;
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
        String os = request.getHeader("os");
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
