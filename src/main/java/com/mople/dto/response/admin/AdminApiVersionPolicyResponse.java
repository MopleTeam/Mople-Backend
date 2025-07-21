package com.mople.dto.response.admin;

public record AdminApiVersionPolicyResponse(
        String os,
        String uri,
        int appVersion,
        String apiVersion,
        String description
) {
}
