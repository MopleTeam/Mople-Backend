package com.groupMeeting.dto.request.meet.review;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ReviewImageRequest(
        Long reviewId,
        List<MultipartFile> images
) {}
