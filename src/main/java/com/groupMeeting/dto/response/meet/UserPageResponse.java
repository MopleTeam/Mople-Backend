package com.groupMeeting.dto.response.meet;

import java.util.List;

public record UserPageResponse(
        List<ReviewPageResponse> reviews,
        List<PlanPageResponse> plans
) {
}
