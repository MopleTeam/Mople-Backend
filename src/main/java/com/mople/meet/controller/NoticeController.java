package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.dto.request.meet.notice.NoticeUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.meet.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
@Tag(name = "NOTICE", description = "공지 API")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "전체 공지 조회 API",
            description = "모임의 공지 목록을 조회합니다."
    )
    @GetMapping("/list/{meetId}")
    public ResponseEntity<CursorPageResponse<NoticeClientResponse>> getMeetNoticeList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(noticeService.getNoticeList(user.id(), meetId, request));
    }

    @Operation(
            summary = "특정 공지 조회 API",
            description = "모임의 특정 공지를 상세 조회합니다."
    )
    @GetMapping("/detail/{noticeId}")
    public ResponseEntity<NoticeClientResponse> getMeetingPlanDetail(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId
    ) {
        var body = noticeService.getSpecNotice(user.id(), noticeId);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "공지 생성 API",
            description = "모임장이 공지를 생성하고, 생성된 공지 정보를 반환합니다."
    )
    @PostMapping("/create")
    public ResponseEntity<NoticeClientResponse> createMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody @Valid NoticeCreateRequest request
    ) {
        var body = noticeService.createNotice(user.id(), request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "공지 수정 API",
            description = "모임장이 공지 내용을 수정하고, 수정된 공지 정보를 반환합니다."
    )
    @PatchMapping("/update/{noticeId}")
    public ResponseEntity<NoticeClientResponse> updateMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId,
            @RequestBody @Valid NoticeUpdateRequest request
    ) {
        var body = noticeService.updateNotice(user.id(), noticeId, request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "공지 삭제 API",
            description = "모임장이 공지를 삭제합니다."
    )
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> deleteMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId
    ) {
        noticeService.removeNotice(user.id(), noticeId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "공지 고정 API",
            description = "모임장이 공지를 고정합니다."
    )
    @PatchMapping("pin/{noticeId}")
    public ResponseEntity<NoticeClientResponse> pinMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(noticeService.pinNotice(user.id(), noticeId));
    }

    @Operation(
            summary = "공지 고정해제 API",
            description = "모임장이 공지를 고정해제합니다."
    )
    @DeleteMapping("pin/{noticeId}")
    public ResponseEntity<NoticeClientResponse> unpinMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(noticeService.unpinNotice(user.id(), noticeId));
    }
}
