package com.mople.dto.request.meet.review;

import java.util.List;

public record ReviewImageDeleteRequest(
        List<String> reviewImages
) {
}
