package com.groupMeeting.dto.response.policy;

public record ForceUpdatePolicyResponse(
        boolean forceUpdate,
        String minVersion,
        String message
) {
}
