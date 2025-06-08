package com.groupMeeting.admin.service;

import com.groupMeeting.dto.response.admin.AdminApiVersionPolicyResponse;
import com.groupMeeting.dto.response.admin.AdminForceUpdatePolicyResponse;
import com.groupMeeting.entity.version.ApiVersionPolicy;
import com.groupMeeting.entity.version.ForceUpdatePolicy;
import com.groupMeeting.version.repository.ApiVersionPolicyRepository;
import com.groupMeeting.version.repository.ForceUpdatePolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final ForceUpdatePolicyRepository forceUpdatePolicyRepository;
    private final ApiVersionPolicyRepository apiVersionPolicyRepository;

    public void saveForceUpdatePolicy(AdminForceUpdatePolicyResponse policyResponse) {
        ForceUpdatePolicy policy = ForceUpdatePolicy.builder()
                .os(policyResponse.getOs())
                .minVersion(policyResponse.getMinVersion())
                .currentVersion(policyResponse.getCurrentVersion())
                .forceUpdate(policyResponse.isForceUpdate())
                .build();

        forceUpdatePolicyRepository.save(policy);
    }

    public List<ForceUpdatePolicy> getAllForceUpdatePolicy() {
        return forceUpdatePolicyRepository.findAll();
    }

    public ForceUpdatePolicy findForceUpdatePolicyById(Long id) {
        return forceUpdatePolicyRepository.findById(id).orElseThrow();
    }

    public void updateForceUpdatePolicy(Long id, AdminForceUpdatePolicyResponse policyResponse) {
        ForceUpdatePolicy policy = findForceUpdatePolicyById(id);
        policy.updatePolicy(
                policyResponse.getOs(),
                policyResponse.getMinVersion(),
                policyResponse.getCurrentVersion(),
                policyResponse.isForceUpdate()
        );

        forceUpdatePolicyRepository.save(policy);
    }

    public void deleteForceUpdatePolicy(Long id) {
        forceUpdatePolicyRepository.deleteById(id);
    }

    public void saveApiVersionPolicy(AdminApiVersionPolicyResponse policyResponse) {
        ApiVersionPolicy policy = ApiVersionPolicy.builder()
                .os(policyResponse.getOs())
                .uri(policyResponse.getUri())
                .appVersion(policyResponse.getAppVersion())
                .apiVersion(policyResponse.getApiVersion())
                .description(policyResponse.getDescription())
                .build();

        apiVersionPolicyRepository.save(policy);
    }

    public List<ApiVersionPolicy> getAllApiVersionPolicy() {
        return apiVersionPolicyRepository.findAll();
    }

    public ApiVersionPolicy findApiVersionPolicyById(Long id) {
        return apiVersionPolicyRepository.findById(id).orElseThrow();
    }

    public void updateApiVersionPolicy(Long id, AdminApiVersionPolicyResponse policyResponse) {
        ApiVersionPolicy policy = findApiVersionPolicyById(id);
        policy.updatePolicy(
                policyResponse.getOs(),
                policyResponse.getUri(),
                policyResponse.getAppVersion(),
                policyResponse.getApiVersion(),
                policyResponse.getDescription()
        );

        apiVersionPolicyRepository.save(policy);
    }

    public void deleteApiVersionPolicy(Long id) {
        apiVersionPolicyRepository.deleteById(id);
    }
}
