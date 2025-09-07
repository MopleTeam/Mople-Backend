package com.mople.dto.request.meet.plan;

import jakarta.validation.constraints.NotNull;

public record PlanDeleteRequest(
        @NotNull Long version
) {}
