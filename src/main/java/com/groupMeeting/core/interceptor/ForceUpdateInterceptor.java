package com.groupMeeting.core.interceptor;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.version.service.ForceUpdatePolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Component
@RequiredArgsConstructor
public class ForceUpdateInterceptor implements HandlerInterceptor {
    private static final String OS_HEADER = "os";
    private static final String VERSION_HEADER = "version";

    private final ForceUpdatePolicyService forceUpdatePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String os = request.getHeader(OS_HEADER);
        String appVersion = request.getHeader(VERSION_HEADER);
        validateHeader(os, appVersion);

        if (forceUpdatePolicyService.isForceUpdateRequired(os, appVersion)) {
            throw new VersionException(FORCE_UPDATE);
        }

        return true;
    }

    private static void validateHeader(String os, String appVersion) {
        if (os == null) {
            throw new VersionException(EMPTY_OS);
        }
        if (appVersion == null) {
            throw new VersionException(EMPTY_VERSION);
        }
    }
}
