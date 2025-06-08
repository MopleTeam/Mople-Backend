package com.groupMeeting.dto.response.pagination;

import java.util.List;

public record CursorPageResponse<T>(List<T> content, CursorPage cursorPage) {

    public static <T> CursorPageResponse<T> of(List<T> content, CursorPage page) {
        return new CursorPageResponse<>(content, page);
    }
}
