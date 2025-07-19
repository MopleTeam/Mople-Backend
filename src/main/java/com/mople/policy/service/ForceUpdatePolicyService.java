package com.mople.policy.service;

import com.mople.core.exception.custom.PolicyException;
import com.mople.dto.client.ForceUpdatePolicyClientResponse;
import com.mople.dto.response.policy.ForceUpdatePolicyResponse;
import com.mople.entity.policy.ForceUpdatePolicy;
import com.mople.global.enums.Os;
import com.mople.global.utils.version.VersionUtils;
import com.mople.policy.repository.ForceUpdatePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mople.dto.client.ForceUpdatePolicyClientResponse.ofForceUpdatePolicy;
import static com.mople.global.enums.ExceptionReturnCode.UNSUPPORTED_OS;

@Service
@RequiredArgsConstructor
public class ForceUpdatePolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;

    @Transactional(readOnly = true)
    public ForceUpdatePolicyClientResponse getForceUpdatePolicy(String osHeader, String versionHeader) {
        Os os = Os.from(osHeader);

        if (os == Os.UNKNOWN || versionHeader == null) {
            return getPolicyResponse(
                    ForceUpdatePolicy.getDefaultForceUpdate(),
                    ForceUpdatePolicy.getDefaultMinVersion(),
                    ForceUpdatePolicy.getDefaultMessage()
            );
        }

        VersionUtils.validateVersionFormat(versionHeader);
        ForceUpdatePolicy policy = findForceUpdatePolicy(os.getValue());

        int versionCode = VersionUtils.convertToVersionCode(versionHeader);

        return getPolicyResponse(
                policy.isForceUpdateRequired(versionCode),
                VersionUtils.convertToVersion(policy.getMinVersion()),
                policy.getForceUpdateMessage(versionCode)
        );
    }

    private ForceUpdatePolicyClientResponse getPolicyResponse(boolean isForceUpdateRequired, String minVersion, String message) {
        return ofForceUpdatePolicy(new ForceUpdatePolicyResponse(
                isForceUpdateRequired,
                minVersion,
                message
        ));
    }

    private ForceUpdatePolicy findForceUpdatePolicy(String os) {
        return forceUpdatePolicyRepository.findByOs(os)
                .orElseThrow(() -> new PolicyException(UNSUPPORTED_OS));
    }
}
