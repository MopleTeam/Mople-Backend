package com.groupMeeting.core.interceptor.version;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.dto.version.VersionInfo;
import com.groupMeeting.version.service.ApiVersionPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_VERSION_POLICY;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiVersionInterceptor implements HandlerInterceptor {
    private final ApiVersionPolicyService apiVersionPolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String os = request.getHeader("os");
        String appVersion = request.getHeader("version");
        String uri = request.getRequestURI();

        String apiVersion = apiVersionPolicyService.findApiVersion(os, uri, appVersion);
        if (apiVersion == null) {
            throw new VersionException(NOT_FOUND_VERSION_POLICY);
        }

        VersionContext.set(new VersionInfo(apiVersion));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        VersionContext.clear();
    }
}
