package com.mople.dto.response.admin;

public record AdminForceUpdatePolicyResponse(
        String os,
        int minVersion,
        int currentVersion,
        boolean forceUpdate,
        String message
) {
}
