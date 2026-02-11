package com.mople.dto.response.meet.plan;

import com.mople.dto.client.PlanClientResponse;
import com.mople.dto.response.meet.MeetListFindMemberResponse;

import java.util.List;

// 강제 업데이트 시 삭제할 것
public record PlanHomeViewResponse_old(
        List<PlanClientResponse> plans,
        List<MeetListFindMemberResponse> meets
) {
}
