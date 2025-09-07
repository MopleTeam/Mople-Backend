package com.mople.dto.request.meet.comment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CommentUpdateRequest(
        @NotNull Long version,
        @NotBlank String contents,
        @Nullable List<Long> mentions
) {
}
