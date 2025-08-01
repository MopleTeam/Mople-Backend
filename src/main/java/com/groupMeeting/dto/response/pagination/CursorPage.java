package com.groupMeeting.dto.response.pagination;

import lombok.Builder;

@Builder
public record CursorPage(
        String nextCursor,
        boolean hasNext,
        int size
) {
}
