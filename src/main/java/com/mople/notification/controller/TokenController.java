package com.mople.notification.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.request.notification.token.TokenCreateRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.notification.service.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "TOKEN", description = "파이어 베이스 토큰 API")
public class TokenController {
    private final TokenService tokenService;

    @Operation(
            summary = "토큰 저장 API",
            description = "Firebase 토큰을 받아 DB에 저장합니다."
    )
    @PostMapping("/save")
    public ResponseEntity<Void> saveToken(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody TokenCreateRequest tokenCreateRequest
    ) {
        tokenService.saveFcmToken(user.id(), tokenCreateRequest);
        return ResponseEntity.ok().build();
    }
}
