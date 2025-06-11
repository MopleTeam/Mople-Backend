package com.groupMeeting.policy.service;

import com.groupMeeting.entity.policy.ApiVersionPolicy;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.policy.repository.impl.ApiVersionPolicySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiVersionPolicyService {
    private final ApiVersionPolicySupport apiVersionPolicySupport;

    @Transactional(readOnly = true)
    public ApiVersionPolicy getApiVersionPolicy(String os, String uri, String appVersion) {
        int versionCode = VersionUtils.convertToVersionCode(appVersion);
        return apiVersionPolicySupport.findApplicablePolicy(os, uri, versionCode).orElse(null);
    }
}
