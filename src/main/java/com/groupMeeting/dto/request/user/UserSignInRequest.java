package com.groupMeeting.dto.request.user;

import com.groupMeeting.global.enums.SocialProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSignInRequest(
        @NotNull SocialProvider socialProvider,
        @NotBlank String providerToken,
        @NotBlank String email
){}
