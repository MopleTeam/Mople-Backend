package com.mople.user.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.UserClientResponse;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.request.user.UserDeleteRequest;
import com.mople.dto.request.user.UserInfoRequest;
import com.mople.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "USER", description = "회원 정보 관리 API")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "유저 정보 API",
            description = "유저 정보를 반환합니다."
    )
    @GetMapping("/info")
    public ResponseEntity<UserClientResponse> getMyInfo(
            @Parameter(hidden = true) @SignUser AuthUserRequest user
    ) {
        return ResponseEntity.ok(userService.getInfo(user.id()));
    }

    @Operation(
            summary = "유저 정보 갱신 API",
            description = "DB에 저장된 유저 정보를 갱신합니다."
    )
    @PatchMapping("/info")
    public ResponseEntity<UserClientResponse> updateInfo(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @Valid @RequestBody UserInfoRequest userInfoRequest
    ) {
        var updateUserInfo = userService.updateInfo(user.id(), userInfoRequest);
        return ResponseEntity.ok(updateUserInfo);
    }

    @Operation(
            summary = "유저 삭제 API",
            description = "DB에서 회원 정보를 삭제합니다."
    )
    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeUser(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @Valid @RequestBody UserDeleteRequest userDeleteRequest
    ) {
        userService.removeUser(user.id(), userDeleteRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "닉네임 생성 API",
            description = "랜덤 닉네임을 생성해 반환합니다."
    )
    @GetMapping("/nickname/random")
    public ResponseEntity<String> randomNickname() {
        return ResponseEntity.ok(userService.randomUserNickname());
    }

    @Operation(
            summary = "닉네임 중복 확인 API",
            description = "DB에 저장된 닉네임을 조회해 중복 여부를 확인합니다."
    )
    @GetMapping("/nickname/duplicate")
    public ResponseEntity<Boolean> duplicateNickname(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(userService.duplicateNickname(nickname));
    }
}