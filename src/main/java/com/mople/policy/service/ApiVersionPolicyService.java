package com.mople.policy.service;

import com.mople.core.exception.custom.PolicyException;
import com.mople.entity.policy.ApiVersionPolicy;
import com.mople.global.enums.Os;
import com.mople.global.utils.version.VersionUtils;
import com.mople.policy.repository.impl.ApiVersionPolicySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_API_VERSION_POLICY;

@Service
@RequiredArgsConstructor
public class ApiVersionPolicyService {
    private final ApiVersionPolicySupport apiVersionPolicySupport;

    @Transactional(readOnly = true)
    public ApiVersionPolicy getApiVersionPolicy(Os os, String uri, String appVersion) {
        if (os == Os.UNKNOWN || appVersion == null) {
            return apiVersionPolicySupport.findDefaultPolicyForUri(uri)
                    .orElseThrow(() -> new PolicyException(NOT_FOUND_API_VERSION_POLICY));
        }

        int versionCode = VersionUtils.convertToVersionCode(appVersion);
        return apiVersionPolicySupport.findApplicablePolicy(os.getValue(), uri, versionCode)
                .orElseThrow(() -> new PolicyException(NOT_FOUND_API_VERSION_POLICY));
    }
}
