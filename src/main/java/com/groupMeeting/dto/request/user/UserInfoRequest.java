package com.groupMeeting.dto.request.user;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserInfoRequest(
        @NotBlank @Pattern(regexp = "^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{2,32}$") String nickname,
        @Nullable String image
) {}