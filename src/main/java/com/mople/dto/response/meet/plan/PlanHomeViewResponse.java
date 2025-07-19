package com.mople.dto.response.meet.plan;

import com.mople.dto.client.PlanClientResponse;
import com.mople.dto.response.meet.MeetListFindMemberResponse;

import java.util.List;

public record PlanHomeViewResponse(
        List<PlanClientResponse> plans,
        List<MeetListFindMemberResponse> meets
) {
}
