package com.mople.meet.controller.comment;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.request.meet.comment.CommentReportRequest;
import com.mople.meet.service.comment.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
            summary = "댓글/답글 삭제 API",
            description = "댓글/답글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteReviewComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(user.id(), commentId);
        return ResponseEntity.ok().build();
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
        commentService.commentReport(user.id(), CommentReportRequest);
        return ResponseEntity.ok().build();
    }
}
