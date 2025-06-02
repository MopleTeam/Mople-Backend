package com.groupMeeting.dto.request.user;

import com.groupMeeting.global.enums.Role;

import jakarta.validation.constraints.NotNull;

public record AuthUserRequest(
        @NotNull Long id,
        @NotNull Role role
) {
    public String securityRole() {
        return role.securityRole();
    }
}
