package com.mople.dto.request.meet.notice;

import jakarta.validation.constraints.NotBlank;

public record NoticeCreateRequest(
        @NotBlank String content
) {}
