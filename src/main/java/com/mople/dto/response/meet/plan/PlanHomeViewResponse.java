package com.mople.dto.response.meet.plan;

import com.mople.dto.client.PlanClientResponse;

import java.util.List;

public record PlanHomeViewResponse(
        List<PlanClientResponse> plans,
        boolean hasJoinedMeet
) {
}
