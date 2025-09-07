package com.mople.dto.request.meet;

import jakarta.validation.constraints.NotNull;

public record MeetUpdateRequest(
        @NotNull Long version,
        String name,
        String image
) {}
