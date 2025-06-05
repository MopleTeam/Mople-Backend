package com.groupMeeting.version.service;

import com.groupMeeting.entity.version.ApiVersionPolicy;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.version.repository.impl.ApiVersionPolicySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApiVersionPolicyService {
    private final ApiVersionPolicySupport apiVersionPolicySupport;

    public ApiVersionPolicy getApiVersionPolicy(String os, String uri, String appVersion) {
        int versionCode = VersionUtils.convertToVersionCode(appVersion);
        return apiVersionPolicySupport.findApplicablePolicy(os, uri, versionCode).orElse(null);
    }
}
