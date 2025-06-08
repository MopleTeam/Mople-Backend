package com.groupMeeting.policy.service;

import com.groupMeeting.core.exception.custom.PolicyException;
import com.groupMeeting.dto.client.ForceUpdatePolicyClientResponse;
import com.groupMeeting.dto.response.policy.ForceUpdatePolicyResponse;
import com.groupMeeting.entity.policy.ForceUpdatePolicy;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.global.utils.version.VersionUtils;
import com.groupMeeting.policy.repository.ForceUpdatePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.groupMeeting.dto.client.ForceUpdatePolicyClientResponse.ofForceUpdatePolicy;
import static com.groupMeeting.global.enums.ExceptionReturnCode.*;

@Service
@RequiredArgsConstructor
public class ForceUpdatePolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;

    @Transactional(readOnly = true)
    public ForceUpdatePolicyClientResponse getForceUpdatePolicy(String os, String version) {
        validateHeader(os, version);
        int versionCode = VersionUtils.convertToVersionCode(version);

        ForceUpdatePolicy policy = findForceUpdatePolicy(os);

        boolean forceUpdateRequired = policy.isForceUpdateRequired(versionCode);
        String minVersion = VersionUtils.convertToVersion(policy.getMinVersion());
        String message = policy.getUpdateMessage(versionCode);

        return ofForceUpdatePolicy(new ForceUpdatePolicyResponse(forceUpdateRequired, minVersion, message));
    }

    private void validateHeader(String os, String version) {
        if (os == null) {
            throw new PolicyException(EMPTY_OS);
        }
        if (version == null) {
            throw new PolicyException(EMPTY_VERSION);
        }
        VersionUtils.validateVersionFormat(version);
    }

    private ForceUpdatePolicy findForceUpdatePolicy(String os) {
        return forceUpdatePolicyRepository.findByOs(os)
                .orElseThrow(() -> new PolicyException(ExceptionReturnCode.UNSUPPORTED_OS));
    }
}
