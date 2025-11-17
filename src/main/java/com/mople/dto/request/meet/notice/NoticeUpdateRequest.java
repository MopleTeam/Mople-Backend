package com.mople.dto.request.meet.notice;

import jakarta.validation.constraints.NotBlank;

public record NoticeUpdateRequest(
        @NotBlank String content
) {}
