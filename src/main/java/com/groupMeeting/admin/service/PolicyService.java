package com.groupMeeting.admin.service;

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

    public void saveForceUpdatePolicy(ForceUpdatePolicy policy) {
        forceUpdatePolicyRepository.save(policy);
    }

    public List<ForceUpdatePolicy> getAllForceUpdatePolicy() {
        return forceUpdatePolicyRepository.findAll();
    }

    public ForceUpdatePolicy findForceUpdatePolicyById(Long id) {
        return forceUpdatePolicyRepository.findById(id).orElseThrow();
    }

    public void updateForceUpdatePolicy(Long id, ForceUpdatePolicy updatedPolicy) {
        ForceUpdatePolicy policy = findForceUpdatePolicyById(id);
        policy.setOs(updatedPolicy.getOs());
        policy.setMinVersion(updatedPolicy.getMinVersion());
        policy.setCurrentVersion(updatedPolicy.getCurrentVersion());
        policy.setForceUpdate(updatedPolicy.isForceUpdate());

        forceUpdatePolicyRepository.save(policy);
    }

    public void deleteForceUpdatePolicy(Long id) {
        forceUpdatePolicyRepository.deleteById(id);
    }

    public void saveApiVersionPolicy(ApiVersionPolicy policy) {
        apiVersionPolicyRepository.save(policy);
    }

    public List<ApiVersionPolicy> getAllApiVersionPolicy() {
        return apiVersionPolicyRepository.findAll();
    }

    public ApiVersionPolicy findApiVersionPolicyById(Long id) {
        return apiVersionPolicyRepository.findById(id).orElseThrow();
    }

    public void updateApiVersionPolicy(Long id, ApiVersionPolicy updatedPolicy) {
        ApiVersionPolicy policy = findApiVersionPolicyById(id);
        policy.setOs(updatedPolicy.getOs());
        policy.setUri(updatedPolicy.getUri());
        policy.setAppVersion(updatedPolicy.getAppVersion());
        policy.setApiVersion(updatedPolicy.getApiVersion());
        policy.setDescription(updatedPolicy.getDescription());

        apiVersionPolicyRepository.save(policy);
    }

    public void deleteApiVersionPolicy(Long id) {
        apiVersionPolicyRepository.deleteById(id);
    }
}
