package com.groupMeeting.core.mapping;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.core.interceptor.version.VersionContext;
import com.groupMeeting.dto.version.VersionInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_VERSION_INFO;

@RequiredArgsConstructor
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {
    private final String apiVersion;

    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        return new ApiVersionCondition(other.apiVersion != null ? other.apiVersion : this.apiVersion);
    }

    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        VersionInfo versionInfo = VersionContext.get();

        if (versionInfo == null) {
            throw new VersionException(NOT_FOUND_VERSION_INFO);
        }

        if (versionInfo.apiVersion().equals(this.apiVersion)) {
            return this;
        }

        return null;
    }

    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        try {
            return Integer.compare(
                    Integer.parseInt(other.apiVersion),
                    Integer.parseInt(this.apiVersion)
            );
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
