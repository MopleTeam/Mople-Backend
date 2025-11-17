package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.NoticeClientResponse;
import com.mople.dto.request.meet.notice.NoticeCreateRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.meet.service.notice.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meet/{meetId}/notices")
@RequiredArgsConstructor
@Tag(name = "NOTICE", description = "공지 API")
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(
            summary = "공지 생성 API",
            description = "모임장이 공지를 생성하고, 생성된 공지 정보를 반환합니다."
    )
    @PostMapping
    public ResponseEntity<NoticeClientResponse> createMeetNotice(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @RequestBody @Valid NoticeCreateRequest request
    ) {
        var body = noticeService.createNotice(user.id(), meetId, request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }
}
