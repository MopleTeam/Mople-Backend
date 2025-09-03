package com.mople.meet.controller;

import com.mople.core.annotation.auth.SignUser;
import com.mople.dto.client.ReviewClientResponse;
import com.mople.dto.client.UserRoleClientResponse;
import com.mople.dto.request.meet.review.ReviewImageDeleteRequest;
import com.mople.dto.request.meet.review.ReviewReportRequest;
import com.mople.dto.request.pagination.CursorPageRequest;
import com.mople.dto.request.user.AuthUserRequest;
import com.mople.dto.response.meet.review.ReviewImageListResponse;
import com.mople.dto.response.pagination.FlatCursorPageResponse;
import com.mople.meet.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@Tag(name = "REVIEW", description = "후기 API")
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(
            summary = "후기 목록 조회 API",
            description = "모임 id를 통해 모임의 모든 후기 목록을 조회합니다."
    )
    @GetMapping("/list/{meetId}")
    public ResponseEntity<FlatCursorPageResponse<ReviewClientResponse>> getReviews(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long meetId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(reviewService.getReviewList(user.id(), meetId, request));
    }

    @Operation(
            summary = "후기 상세 조회 API",
            description = "후기 review Id로 상세 조회합니다."
    )
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewClientResponse> getReviewDetail(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReviewDetail(reviewId));
    }

    @Operation(
            summary = "후기 상세 조회 API",
            description = "후기 Post Id로 상세 조회합니다."
    )
    @GetMapping("/post/{postId}")
    public ResponseEntity<ReviewClientResponse> getReviewDetailByPostId(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(reviewService.getReviewDetailByPost(postId));
    }

    @Operation(
            summary = "후기 삭제 API",
            description = "후기 ID를 통해 후기를 삭제합니다."
    )
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> removeReview(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long reviewId,
            @RequestParam Long version
    ) {
        reviewService.removeReview(user.id(), reviewId, version);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "후기 참가 유저 조회 API",
            description = "후기에 참가하는 유저 정보를 반환합니다."
    )
    @GetMapping("/participants/{reviewId}")
    public ResponseEntity<FlatCursorPageResponse<UserRoleClientResponse>> getReviewParticipants(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long reviewId,
            @ParameterObject @Valid CursorPageRequest request
    ) {
        return ResponseEntity.ok(reviewService.getParticipantList(user.id(), reviewId, request));
    }

    @Operation(
            summary = "후기 이미지 조회 API",
            description = "후기에 업로드 된 모든 이미지를 반환합니다."
    )
    @GetMapping("/images/{reviewId}")
    public ResponseEntity<List<ReviewImageListResponse>> getReviewImages(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReviewImages(reviewId));
    }

    @Operation(
            summary = "후기 이미지 삭제 API",
            description = "이미지 id를 제공받아 업로드 된 이미지를 삭제하고 삭제되지 않은 이미지 목록을 반환합니다.."
    )
    @DeleteMapping("/images/{reviewId}")
    public ResponseEntity<List<ReviewImageListResponse>> deleteReviewImages(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @PathVariable Long reviewId,
            @RequestBody ReviewImageDeleteRequest reviewImageDeleteRequest
    ) {
        return ResponseEntity.ok(reviewService.removeReviewImages(user.id(), reviewId, reviewImageDeleteRequest));
    }

    @Operation(
            summary = "후기 신고 API",
            description = "유저가 신고한 후기를 저장하고 Admin Page에서 관리합니다."
    )
    @PostMapping("/report")
    public ResponseEntity<Void> reportComment(
            @Parameter(hidden = true) @SignUser AuthUserRequest user,
            @RequestBody ReviewReportRequest reviewReportRequest
    ) {
        reviewService.reviewReport(user.id(), reviewReportRequest);
        return ResponseEntity.ok().build();
    }
}