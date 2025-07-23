package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.MeetClientResponse;
import com.mople.dto.request.meet.MeetCreateRequest;
import com.mople.dto.request.meet.MeetUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.client.MeetMemberClientResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.meet.service.MeetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meet")
@RequiredArgsConstructor
@Tag(name = "MEET", description = "모임 API")
public class MeetController {
    private final MeetService meetService;

    @Operation(
            summary = "모임 생성 API",
            description = "모임 이름과 이미지를 받아 모임 생성 정보를 반환합니다."
    )
    @PostMapping("/create")
    public ResponseEntity<MeetClientResponse> create(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody MeetCreateRequest create
    ) {
        return ResponseEntity.ok(meetService.createMeet(user.id(), create));
    }

    @Operation(
            summary = "모임 수정 API",
            description = "모임 아이디와 이름, 이미지를 받아 모임 수정 정보를 반환합니다."
    )
    @PatchMapping("/update/{meetId}")
    public ResponseEntity<MeetClientResponse> update(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @RequestBody MeetUpdateRequest updateRequest
    ) {
        return ResponseEntity.ok(meetService.updateMeet(user.id(), meetId, updateRequest));
    }

    @Operation(
            summary = "모임 조회 API",
            description = "유저의 모임 목록을 조회합니다."
    )
    @GetMapping("/list")
    public ResponseEntity<CursorPageResponse<MeetClientResponse>> getMeetList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(meetService.getUserMeetList(user.id(), request));
    }

    @Operation(
            summary = "모임 세부 조회 API",
            description = "모임 세부 정보를 조회합니다."
    )
    @GetMapping("/{meetId}")
    public ResponseEntity<MeetClientResponse> getMeetInfo(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId
    ) {
        return ResponseEntity.ok(meetService.getMeetDetail(meetId, user.id()));
    }

    @Operation(
            summary = "모임 유저 목록 조회 API",
            description = "모임 세부 정보를 조회합니다."
    )
    @GetMapping("/members/{meetId}")
    public ResponseEntity<MeetMemberClientResponse> getMeetMembers(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(meetService.meetMemberList(meetId, user.id(), request));
    }

    @Operation(
            summary = "모임 탈퇴 API",
            description = "모임에서 모임 생성자라면 모임을 삭제하고, 생성자가 아니면 모임과 일정에서 삭제됩니다. "
    )
    @DeleteMapping("/{meetId}")
    public ResponseEntity<Void> deleteMeet(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId
    ) {
        meetService.removeMeet(meetId, user.id());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "모임 초대 API",
            description = "meetId를 받아서 초대 링크를 생성합니다."
    )
    @PostMapping("/invite/{meetId}")
    public ResponseEntity<String> createInvite(
            @PathVariable Long meetId
    ) {
        return ResponseEntity.ok(meetService.createInvite(meetId));
    }

    @Operation(
            summary = "모임 가입 API",
            description = "UUID를 조회해 초대 받은 모임에 가입합니다."
    )
    @PostMapping("/join/{meetCode}")
    public ResponseEntity<MeetClientResponse> joinMeetMember(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable String meetCode
    ) {
        return ResponseEntity.ok(meetService.meetJoinMember(user.id(), meetCode));
    }
}
