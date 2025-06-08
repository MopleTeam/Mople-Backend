package com.groupMeeting.admin.service;

import com.groupMeeting.core.exception.custom.PolicyException;
import com.groupMeeting.dto.response.admin.AdminApiVersionPolicyResponse;
import com.groupMeeting.dto.response.admin.AdminForceUpdatePolicyResponse;
import com.groupMeeting.entity.policy.ApiVersionPolicy;
import com.groupMeeting.entity.policy.ForceUpdatePolicy;
import com.groupMeeting.policy.repository.ApiVersionPolicyRepository;
import com.groupMeeting.policy.repository.ForceUpdatePolicyRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_API_VERSION_POLICY;
import static com.groupMeeting.global.enums.ExceptionReturnCode.NOT_FOUND_FORCE_UPDATE_POLICY;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;
    private final ApiVersionPolicyRepository apiVersionPolicyRepository;

    @Transactional
    public void saveForceUpdatePolicy(AdminForceUpdatePolicyResponse policyResponse) {
        ForceUpdatePolicy policy = ForceUpdatePolicy.builder()
                .os(policyResponse.os())
                .minVersion(policyResponse.minVersion())
                .currentVersion(policyResponse.currentVersion())
                .forceUpdate(policyResponse.forceUpdate())
                .message(policyResponse.message())
                .build();

        forceUpdatePolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<ForceUpdatePolicy> getAllForceUpdatePolicy() {
        return forceUpdatePolicyRepository.findAll();
    }

    public ForceUpdatePolicy findForceUpdatePolicyById(Long id) {
        return forceUpdatePolicyRepository.findById(id)
                .orElseThrow(() -> new PolicyException(NOT_FOUND_FORCE_UPDATE_POLICY));
    }

    @Transactional
    public void updateForceUpdatePolicy(Long id, AdminForceUpdatePolicyResponse policyResponse) {
        ForceUpdatePolicy policy = findForceUpdatePolicyById(id);
        policy.updatePolicy(
                policyResponse.os(),
                policyResponse.minVersion(),
                policyResponse.currentVersion(),
                policyResponse.forceUpdate(),
                policyResponse.message()
        );

        forceUpdatePolicyRepository.save(policy);
    }

    @Transactional
    public void deleteForceUpdatePolicy(Long id) {
        forceUpdatePolicyRepository.deleteById(id);
    }

    @Transactional
    public void saveApiVersionPolicy(AdminApiVersionPolicyResponse policyResponse) {
        ApiVersionPolicy policy = ApiVersionPolicy.builder()
                .os(policyResponse.os())
                .uri(policyResponse.uri())
                .appVersion(policyResponse.appVersion())
                .apiVersion(policyResponse.apiVersion())
                .description(policyResponse.description())
                .build();

        apiVersionPolicyRepository.save(policy);
    }

    @Transactional(readOnly = true)
    public List<ApiVersionPolicy> getAllApiVersionPolicy() {
        return apiVersionPolicyRepository.findAll();
    }

    public ApiVersionPolicy findApiVersionPolicyById(Long id) {
        return apiVersionPolicyRepository.findById(id)
                .orElseThrow(() -> new PolicyException(NOT_FOUND_API_VERSION_POLICY));
    }

    @Transactional
    public void updateApiVersionPolicy(Long id, AdminApiVersionPolicyResponse policyResponse) {
        ApiVersionPolicy policy = findApiVersionPolicyById(id);
        policy.updatePolicy(
                policyResponse.os(),
                policyResponse.uri(),
                policyResponse.appVersion(),
                policyResponse.apiVersion(),
                policyResponse.description()
        );

        apiVersionPolicyRepository.save(policy);
    }

    @Transactional
    public void deleteApiVersionPolicy(Long id) {
        apiVersionPolicyRepository.deleteById(id);
    }
}
