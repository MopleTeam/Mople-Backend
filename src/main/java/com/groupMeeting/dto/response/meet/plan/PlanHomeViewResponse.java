package com.groupMeeting.dto.response.meet.plan;

import com.groupMeeting.dto.client.PlanClientResponse;
import com.groupMeeting.dto.response.meet.MeetListFindMemberResponse;

import java.util.List;

public record PlanHomeViewResponse(
        List<PlanClientResponse> plans,
        List<MeetListFindMemberResponse> meets
) {
}
