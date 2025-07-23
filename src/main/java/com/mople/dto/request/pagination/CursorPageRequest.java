package com.mople.dto.request.pagination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CursorPageRequest(
        @Min(1) @Max(100) Integer size,
        String cursor
) {
    public int getSafeSize() {
        return size != null ? size : 10;
    }
}
