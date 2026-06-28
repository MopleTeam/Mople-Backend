package com.mople.dto.request.meet.comment;

import jakarta.validation.constraints.NotBlank;

public record NoticeCommentUpdateRequest(
        @NotBlank String contents
) {
}
