package com.mople.dto.request.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CursorPageRequest(
        @Min(1) @Max(100) Integer size,
        String cursor
) {

    private static final int DEFAULT_SIZE = 10;

    public int getSafeSize() {
        return size != null ? size : DEFAULT_SIZE;
    }
}
