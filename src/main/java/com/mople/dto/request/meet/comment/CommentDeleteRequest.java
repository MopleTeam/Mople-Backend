package com.mople.dto.request.meet.comment;

import jakarta.validation.constraints.NotNull;

public record CommentDeleteRequest(
        @NotNull Long version
) {
}
