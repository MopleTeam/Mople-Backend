package com.mople.dto.response.pagination;

import java.util.List;

public record FlatCursorPageResponse<T>(
        Long totalCount,
        List<T> content,
        CursorPage page
) {
    public static <T> FlatCursorPageResponse<T> of(Long totalCount, CursorPageResponse<T> response) {
        return new FlatCursorPageResponse<>(
                totalCount,
                response.content(),
                response.page()
        );
    }
}
