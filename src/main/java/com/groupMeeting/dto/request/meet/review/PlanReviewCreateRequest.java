package com.groupMeeting.dto.request.meet.review;

public record PlanReviewCreateRequest(
    Long planId,
    String contents
) {
}
