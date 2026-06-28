package com.mople.meet.controller.comment;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.comment.PostCommentClientResponse;
import com.mople.dto.request.meet.comment.CommentCreateRequest;
import com.mople.dto.request.meet.comment.PostCommentUpdateRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.pagination.CursorPageResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.meet.service.comment.PostCommentService;
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
@RequestMapping("/comment/post")
@Tag(name = "POST_COMMENT", description = "게시글 댓글 API")
public class PostCommentController {

    private final PostCommentService postCommentService;

    @Operation(
            summary = "게시글 댓글 조회 API",
            description = "게시글 모든 댓글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}")
    public ResponseEntity<FlatCursorPageResponse<PostCommentClientResponse>> commentList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(postCommentService.getPostCommentList(user.id(), postId, request));
    }

    @Operation(
            summary = "게시글 답글 조회 API",
            description = "게시글 모든 답글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}/{commentId}")
    public ResponseEntity<CursorPageResponse<PostCommentClientResponse>> commentList(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(postCommentService.getPostCommentReplyList(user.id(), postId, commentId, request));
    }

    @Operation(
            summary = "게시글 댓글 생성 API",
            description = "게시글 댓글을 작성합니다."
    )
    @PostMapping("/{postId}")
    public ResponseEntity<PostCommentClientResponse> createComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        var body = postCommentService.createPostComment(user.id(), postId, request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "게시글 답글 생성 API",
            description = "게시글 답글을 작성합니다."
    )
    @PostMapping("/{postId}/{commentId}")
    public ResponseEntity<PostCommentClientResponse> createCommentReply(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest request
    ) {
        var body = postCommentService.createPostCommentReply(user.id(), postId, commentId, request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "게시글 댓글/답글 수정 API",
            description = "댓글 ID를 통해 게시글 댓글/답글을 수정합니다."
    )
    @PatchMapping("/{commentId}")
    public ResponseEntity<PostCommentClientResponse> updateComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId,
            @RequestBody @Valid PostCommentUpdateRequest request
    ) {
        var body = postCommentService.updatePostComment(user.id(), commentId, request);

        return ResponseEntity.ok()
                .eTag("\"" + body.getVersion() + "\"")
                .body(body);
    }

    @Operation(
            summary = "게시글 댓글/답글 좋아요 토글 API",
            description = "댓글/답글에 좋아요를 추가하거나 취소합니다."
    )
    @PostMapping("/{commentId}/likes")
    public ResponseEntity<PostCommentClientResponse> toggleCommentLike(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId
    ) {
        return ResponseEntity.ok(postCommentService.toggleLike(user.id(), commentId));
    }
}
