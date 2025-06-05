package com.groupMeeting.version.service;

import com.groupMeeting.core.exception.custom.VersionException;
import com.groupMeeting.entity.version.ForceUpdatePolicy;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.version.repository.ForceUpdatePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.groupMeeting.global.enums.ExceptionReturnCode.UNSUPPORTED_VERSION;

@Service
@RequiredArgsConstructor
public class ForceUpdatePolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;

    public boolean isForceUpdateRequired(String os, String version) {
        ForceUpdatePolicy policy = findForceUpdatePolicy(os);
        int versionCode = convertToVersionCode(version);

        return versionCode < policy.getMinVersion() && policy.isForceUpdate();
    }

    private ForceUpdatePolicy findForceUpdatePolicy(String os) {
        return forceUpdatePolicyRepository.findByOs(os)
                .orElseThrow(() -> new VersionException(ExceptionReturnCode.UNSUPPORTED_OS));
    }

    private int convertToVersionCode(String version) {
        String[] versionParts = version.split("\\.");
        for (String part : versionParts) {
            try {
                Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new VersionException(UNSUPPORTED_VERSION);
            }
        }

        return VersionUtils.convertToVersionCode(version);
    }
}
