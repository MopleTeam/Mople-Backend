package com.groupMeeting.dto.response.version;

public record ForceUpdatePolicyResponse(
        boolean forceUpdate,
        String minVersion,
        String message
) {
}
