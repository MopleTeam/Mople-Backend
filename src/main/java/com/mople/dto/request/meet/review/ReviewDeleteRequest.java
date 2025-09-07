package com.mople.dto.request.meet.review;

import jakarta.validation.constraints.NotNull;

public record ReviewDeleteRequest(
        @NotNull Long version
) {}
