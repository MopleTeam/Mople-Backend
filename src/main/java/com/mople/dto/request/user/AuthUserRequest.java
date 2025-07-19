package com.mople.dto.request.user;

import com.mople.global.enums.Role;

import jakarta.validation.constraints.NotNull;

public record AuthUserRequest(
        @NotNull Long id,
        @NotNull Role role
) {
    public String securityRole() {
        return role.securityRole();
    }
}
