package com.groupMeeting.meet.controller;

import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.core.annotation.version.ApiVersion;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.response.pagination.CursorPageResponse;
import com.groupMeeting.meet.service.CommentService;
import com.groupMeeting.dto.request.meet.comment.CommentCreateRequest;
import com.groupMeeting.dto.request.meet.comment.CommentReportRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
@Tag(name = "COMMENT", description = "댓글 API")
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "댓글 조회 API",
            description = "모든 댓글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}")
    @ApiVersion("v1.5")
    public ResponseEntity<CursorPageResponse<CommentClientResponse>> commentList(
            @PathVariable Long postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentList(postId, cursor, size));
    }

    @Operation(
            summary = "일정 댓글 생성 API",
            description = "댓글을 작성합니다."
    )
    @PostMapping("/{postId}")
    @ApiVersion("v1_5")
    public ResponseEntity<CommentClientResponse> createComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        return ResponseEntity.ok(commentService.createComment(user.id(), postId, commentCreateRequest.contents()));
    }

    @Operation(
            summary = "댓글 수정 - API",
            description = "댓글 ID를 통해 댓글을 수정합니다."
    )
    @PatchMapping("/{postId}/{commentId}")
    @ApiVersion("v1.5")
    public ResponseEntity<CommentClientResponse> updateComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        return ResponseEntity.ok(commentService.updateComment(user.id(), postId, commentId, commentCreateRequest.contents()));
    }

    @Operation(
            summary = "댓글 삭제 API",
            description = "댓글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteReviewComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId
    ) {
        commentService.deleteMeetingPlanComment(user.id(), commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "댓글 신고 API",
            description = "유저가 댓글을 신고하고 Admin Page에서 조회합니다."
    )
    @PostMapping("/report")
    public ResponseEntity<Void> reportComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody CommentReportRequest CommentReportRequest
    ) {
        commentService.commentReport(user.id(), CommentReportRequest);
        return ResponseEntity.ok().build();
    }
}