package com.groupMeeting.dto.request.meet.review;

import java.util.List;

public record PlanReviewUpdateRequest (
    Long planId,
    String content,
    List<Long> removeImages
){
}

