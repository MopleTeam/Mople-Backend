package com.mople.dto.request.meet.comment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CommentUpdateRequest(
        @NotBlank String contents,
        @Nullable List<Long> mentions
) {
}
