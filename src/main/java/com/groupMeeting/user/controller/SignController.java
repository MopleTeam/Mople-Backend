package com.groupMeeting.user.controller;

import com.groupMeeting.core.annotation.auth.RefreshToken;
import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.request.user.UserSignInRequest;
import com.groupMeeting.dto.request.user.UserSignUpRequest;
import com.groupMeeting.dto.response.token.TokenResponse;
import com.groupMeeting.user.service.SignService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "AUTH", description = "유저 가입, 로그인, Token 재발행 API")
public class SignController {
    private final SignService signService;

    @Operation(
            summary = "회원 가입 API",
            description = "회원 정보를 DB에 저장합니다."
    )
    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponse> signUp(
            @Valid @RequestBody UserSignUpRequest sign
    ) {
        return ResponseEntity.ok(signService.signUp(sign));
    }

    @Operation(
            summary = "로그인 API",
            description = "로그인을 진행합니다. Email과 Provider가 DB에 있다면 토큰을 반환하고, 없다면 예외를 반환합니다."
    )
    @PostMapping("/sign-in")
    public ResponseEntity<TokenResponse> signIn(
            @Valid @RequestBody UserSignInRequest sign
    ) {
        return ResponseEntity.ok(signService.signIn(sign));
    }

    @Operation(
            summary = "로그아웃 API",
            description = "로그 아웃 API입니다. FCM TOKEN을 비활성화 합니다."
    )
    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(
            @SignUser AuthUserRequest user
    ) {
        signService.signOut(user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "토큰 재 생성 API",
            description = "헤더에 RefreshToken을 담아 요청하면 검증 후 새로 생성된 토큰을 반환합니다."
    )
    @PostMapping("/recreate")
    public ResponseEntity<TokenResponse> recreateToken(
            @RefreshToken String refreshToken
    ) {
        return ResponseEntity.ok(signService.recreateToken(refreshToken));
    }
}
