package com.groupMeeting.dto.request.user;

import com.groupMeeting.global.enums.DeviceType;
import com.groupMeeting.global.enums.SocialProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequest(
        @NotNull(message = "소셜 제공자를 입력해주세요") SocialProvider socialProvider,
        @NotBlank(message = "소셜 토큰을 입력해주세요") String providerToken,
        @NotBlank(message = "email을 입력해주세요") String email,
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-zA-Z0-9가-힣])[a-zA-Z0-9가-힣]{2,16}$",
                message = "닉네임은 2자 이상 16자 이하, 자음과 초성은 허용되지 않습니다."
        )
        String nickname,
        @NotNull(message = "디바이스 타입을 입력해주세요") DeviceType deviceType,
        String image
){}