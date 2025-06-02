package com.groupMeeting.dto.request.meet.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank String contents
) {
}
