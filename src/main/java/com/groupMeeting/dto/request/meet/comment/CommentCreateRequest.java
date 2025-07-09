package com.groupMeeting.dto.request.meet.comment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CommentCreateRequest(
        @NotBlank String contents,
        @Nullable List<Long> mentions
) {
}
