package com.mople.image.controller;

import com.mople.dto.request.meet.review.ReviewImageRequest;
import com.mople.image.service.ImageService;
import com.mople.meet.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/image")
@RequiredArgsConstructor
@Tag(name = "IMAGE", description = "이미지 저장(S3) API")
public class ImageController {
    private final ImageService imageService;
    private final ReviewService reviewService;

    @Operation(
            summary = "이미지 업로드 API",
            description = "S3 버켓에 이미지를 저장 후 URL을 반환합니다."
    )
    @PostMapping("/upload/{folder}")
    public ResponseEntity<String> imageUpload(
            @PathVariable String folder,
            @RequestPart MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(folder, image));
    }

    @Operation(
            summary = "리뷰 이미지 업로드 API",
            description = "이미지와 Plan, Reivew Id를 Json으로 받아 S3에 이미지를 업로드 후 DB에 URL을 저장합니다."
    )
    @PostMapping("/review/{folder}")
    public ResponseEntity<List<String>> reviewImageUpload(
            @PathVariable String folder,
            ReviewImageRequest reviewImageRequest
    ) {

        return ResponseEntity.ok(
                reviewService.storeReviewImages(
                        imageService.uploadImages(folder, reviewImageRequest),
                        reviewImageRequest.reviewId()
                )
        );
    }
}