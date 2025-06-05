package com.groupMeeting.version.service;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.entity.version.ApiVersionPolicy;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.version.repository.impl.ApiVersionPolicySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class ApiVersionPolicyService {
    private final ApiVersionPolicySupport apiVersionPolicySupport;

    public String findApiVersion(String os, String uri, String appVersion) {
        int versionCode = VersionUtils.convertToVersionCode(appVersion);
        ApiVersionPolicy apiVersionPolicy = getApiVersionPolicy(os, uri, versionCode);

        return apiVersionPolicy.getApiVersion();
    }

    private ApiVersionPolicy getApiVersionPolicy(String os, String uri, int userVersion) {
        return apiVersionPolicySupport.findApplicablePolicy(os, uri, userVersion)
                .orElseThrow(() -> new VersionException(NOT_FOUND_VERSION_POLICY));
    }
}
