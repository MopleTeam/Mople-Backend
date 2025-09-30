package com.mople.dto.response.pagination;

import java.util.List;

public record FlatCursorPageResponse<T>(
        Integer totalCount,
        List<T> content,
        CursorPage page
) {
    public static <T> FlatCursorPageResponse<T> of(Integer totalCount, CursorPageResponse<T> response) {
        return new FlatCursorPageResponse<>(
                totalCount,
                response.content(),
                response.page()
        );
    }
}
