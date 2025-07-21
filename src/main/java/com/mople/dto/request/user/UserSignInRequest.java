package com.mople.dto.request.user;

import com.mople.global.enums.SocialProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserSignInRequest(
        @NotNull SocialProvider socialProvider,
        @NotBlank String providerToken,
        @NotBlank String email
){}
