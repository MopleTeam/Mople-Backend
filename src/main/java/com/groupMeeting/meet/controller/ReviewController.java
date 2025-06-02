package com.groupMeeting.meet.controller;

import com.groupMeeting.core.annotation.auth.SignUser;
import com.groupMeeting.dto.client.ReviewClientResponse;
import com.groupMeeting.dto.request.meet.review.ReviewImageDeleteRequest;
import com.groupMeeting.dto.request.meet.review.ReviewReportRequest;
import com.groupMeeting.dto.request.user.AuthUserRequest;
import com.groupMeeting.dto.response.meet.review.ReviewImageListResponse;
import com.groupMeeting.dto.response.meet.review.ReviewParticipantResponse;
import com.groupMeeting.meet.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

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
            summary = "후기 조회 API",
            description = "모임 id를 통해 모임의 모든 후기 목록을 조회합니다."
    )
    @GetMapping("/list/{meetId}")
    public ResponseEntity<List<ReviewClientResponse>> getReviews(
            @PathVariable Long meetId
    ) {
        return ResponseEntity.ok(reviewService.getAllMeetReviews(meetId));
    }

    @Operation(
            summary = "후기 상세 조회 API",
            description = "후기 Post Id로 상세 조회합니다."
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
            @PathVariable Long reviewId
    ) {
        reviewService.removeReview(reviewId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "후기 참여자 조회 API",
            description = "후기 Post Id로 상세 조회합니다."
    )
    @GetMapping("/participant/{reviewId}")
    public ResponseEntity<ReviewParticipantResponse> getReviewParticipants(
            @PathVariable Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.getReviewParticipants(reviewId));
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
            @PathVariable Long reviewId,
            @RequestBody ReviewImageDeleteRequest reviewImageDeleteRequest
    ) {
        return ResponseEntity.ok(reviewService.removeReviewImages(reviewId, reviewImageDeleteRequest));
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