package com.mople.meet.controller.comment;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.comment.NoticeCommentClientResponse;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.meet.service.comment.NoticeCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment/notice")
@Tag(name = "NOTICE_COMMENT", description = "공지 댓글 API")
public class NoticeCommentController {

    private final NoticeCommentService noticeCommentService;

    @Operation(
            summary = "공지 댓글 조회 API",
            description = "공지 모든 댓글을 조회합니다."
    )
    @GetMapping("/{noticeId}")
    public ResponseEntity<FlatCursorPageResponse<NoticeCommentClientResponse>> commentList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(noticeCommentService.getNoticeCommentList(user.id(), noticeId, request));
    }

    @Operation(
            summary = "공지 댓글 생성 API",
            description = "공지 댓글을 작성합니다."
    )
    @PostMapping("/{noticeId}")
    public ResponseEntity<NoticeCommentClientResponse> createComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long noticeId,
            @RequestBody @Valid CommentCreateRequest commentCreateRequest
    ) {
        var body = noticeCommentService.createNoticeComment(user.id(), noticeId, commentCreateRequest);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }
}
