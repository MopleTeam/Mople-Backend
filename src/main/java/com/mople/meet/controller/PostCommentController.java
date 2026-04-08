package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.CommentClientResponse;
import com.mople.dto.request.meet.comment.CommentUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.meet.service.comment.PostCommentService;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.meet.comment.CommentReportRequest;

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
@RequestMapping("/comment")
@Tag(name = "COMMENT", description = "게시글 댓글 API")
public class PostCommentController {
    private final PostCommentService postCommentService;

    @Operation(
            summary = "게시글 댓글 조회 API",
            description = "게시글 모든 댓글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}")
    public ResponseEntity<FlatCursorPageResponse<CommentClientResponse>> commentList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(postCommentService.getCommentList(user.id(), postId, request));
    }

    @Operation(
            summary = "게시글 답글 조회 API",
            description = "게시글 모든 답글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}/{commentId}")
    public ResponseEntity<CursorPageResponse<CommentClientResponse>> commentList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(postCommentService.getCommentReplyList(user.id(), postId, commentId, request));
    }

    @Operation(
            summary = "게시글 댓글 생성 API",
            description = "게시글 댓글을 작성합니다."
    )
    @PostMapping("/{postId}")
    public ResponseEntity<CommentClientResponse> createComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest commentCreateRequest
    ) {
        var body = postCommentService.createComment(user.id(), postId, commentCreateRequest);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "게시글 답글 생성 API",
            description = "게시글 답글을 작성합니다."
    )
    @PostMapping("/{postId}/{commentId}")
    public ResponseEntity<CommentClientResponse> createCommentReply(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest commentCreateRequest
    ) {
        var body = postCommentService.createCommentReply(user.id(), postId, commentId, commentCreateRequest);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "게시글 댓글/답글 수정 API",
            description = "게시글 댓글 ID를 통해 댓글/답글을 수정합니다."
    )
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentClientResponse> updateComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequest commentUpdateRequest
    ) {
        var body = postCommentService.updateComment(user.id(), commentId, commentUpdateRequest);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "댓글/답글 삭제 API",
            description = "댓글/답글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteReviewComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId
    ) {
        postCommentService.deleteComment(user.id(), commentId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "게시글 댓글/답글 좋아요 토글 API",
            description = "댓글/답글에 좋아요를 추가하거나 취소합니다."
    )
    @PostMapping("/{commentId}/likes")
    public ResponseEntity<CommentClientResponse> toggleCommentLike(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(postCommentService.toggleLike(user.id(), commentId));
    }

    @Operation(
            summary = "댓글/답글 신고 API",
            description = "유저가 게시글 댓글/답글을 신고하고 Admin Page에서 조회합니다."
    )
    @PostMapping("/report")
    public ResponseEntity<Void> reportComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody CommentReportRequest CommentReportRequest
    ) {
        postCommentService.commentReport(user.id(), CommentReportRequest);
        return ResponseEntity.ok().build();
    }
}