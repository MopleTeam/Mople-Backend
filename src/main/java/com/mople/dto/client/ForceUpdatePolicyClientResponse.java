package com.mople.dto.client;

import com.mople.dto.response.policy.ForceUpdatePolicyResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ForceUpdatePolicyClientResponse {
    private final boolean forceUpdate;
    private final String minVersion;
    private final String message;

    public static ForceUpdatePolicyClientResponse ofForceUpdatePolicy(ForceUpdatePolicyResponse forceUpdatePolicyResponse) {
        return ForceUpdatePolicyClientResponse.builder()
                .forceUpdate(forceUpdatePolicyResponse.forceUpdate())
                .minVersion(forceUpdatePolicyResponse.minVersion())
                .message(forceUpdatePolicyResponse.message())
                .build();
    }
}
