package com.groupMeeting.core.interceptor.version;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.version.service.ForceUpdatePolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForceUpdateInterceptor implements HandlerInterceptor {
    private final ForceUpdatePolicyService forceUpdatePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String os = request.getHeader("os");
        String appVersion = request.getHeader("version");
        validateHeader(os, appVersion);

        if (forceUpdatePolicyService.isForceUpdateRequired(os, appVersion)) {
            log.info("강제 업데이트 필요");
            throw new VersionException(FORCE_UPDATE);
        }

        return true;
    }

    private static void validateHeader(String os, String appVersion) {
        if (os == null) {
            throw new VersionException(EMPTY_OS);
        }

        if (!os.equalsIgnoreCase("ios") && !os.equalsIgnoreCase("android")) {
            throw new VersionException(UNSUPPORTED_OS);
        }

        if (appVersion == null) {
            throw new VersionException(EMPTY_VERSION);
        }

        String[] versionParts = appVersion.split("\\.");
        for (String part : versionParts) {
            try {
                Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new VersionException(UNSUPPORTED_VERSION);
            }
        }
    }

}
