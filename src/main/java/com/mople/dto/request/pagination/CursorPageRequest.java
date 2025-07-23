package com.mople.dto.request.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CursorPageRequest(
        @Min(1) @Max(100) Integer size,
        String cursor
) {

    private static final int DEFAULT_SIZE = 10;

    @Parameter(hidden = true)
    public int getSafeSize() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
