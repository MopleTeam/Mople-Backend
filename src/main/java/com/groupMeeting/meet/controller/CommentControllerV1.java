package com.groupMeeting.meet.controller;

import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.core.annotation.version.ApiVersion;
import com.groupMeeting.dto.client.CommentClientResponse;
import com.groupMeeting.dto.request.meet.comment.CommentCreateRequest;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.meet.service.CommentServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
@ApiVersion("v1")
@Tag(name = "COMMENT", description = "댓글 API")
public class CommentControllerV1 {
    private final CommentServiceV1 commentService;

    @Operation(
            summary = "댓글 조회 API",
            description = "모든 댓글을 조회합니다. 후기의 경우 후기의 ID가 아닌 Post Id를 Path Variable로 전송합니다."
    )
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentClientResponse>> commentList(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(commentService.getCommentList(postId));
    }

    @Operation(
            summary = "일정 댓글 생성 API",
            description = "댓글을 작성하고 생성된 댓글 목록을 반환합니다."
    )
    @PostMapping("/{postId}")
    public ResponseEntity<List<CommentClientResponse>> createComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        return ResponseEntity.ok(commentService.createComment(user.id(), postId, commentCreateRequest.contents()));
    }

    @Operation(
            summary = "댓글 수정 - API",
            description = "댓글 ID를 통해 댓글을 수정하고 댓글 목록을 반환합니다."
    )
    @PatchMapping("/{postId}/{commentId}")
    public ResponseEntity<List<CommentClientResponse>> updateComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest commentCreateRequest
    ) {
        return ResponseEntity.ok(commentService.updateComment(user.id(), postId, commentId, commentCreateRequest.contents()));
    }
}
