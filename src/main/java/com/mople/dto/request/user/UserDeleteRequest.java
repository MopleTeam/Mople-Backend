package com.mople.dto.request.user;

import jakarta.validation.constraints.NotNull;

public record UserDeleteRequest(
        @NotNull Long version
) {}