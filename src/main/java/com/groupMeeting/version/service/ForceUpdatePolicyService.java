package com.groupMeeting.version.service;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.entity.version.ForceUpdatePolicy;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.version.repository.ForceUpdatePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ForceUpdatePolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;

    public boolean isForceUpdateRequired(String os, String version) {
        ForceUpdatePolicy policy = forceUpdatePolicyRepository.findByOs(os)
                .orElseThrow(() -> new VersionException(ExceptionReturnCode.UNSUPPORTED_OS));

        int versionCode = VersionUtils.convertToVersionCode(version);
        return versionCode < policy.getMinVersion() && policy.isForceUpdate();
    }
}
