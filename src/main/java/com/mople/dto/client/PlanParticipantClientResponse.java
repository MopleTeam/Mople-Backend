package com.mople.dto.client;

import com.mople.dto.response.meet.plan.PlanParticipantResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlanParticipantClientResponse {
    private final Long creatorId;
    private final CursorPageResponse<PlanParticipantResponse> members;

    public static PlanParticipantClientResponse ofParticipants(Long creatorId, CursorPageResponse<PlanParticipantResponse> members) {
        return PlanParticipantClientResponse.builder()
                .creatorId(creatorId)
                .members(members)
                .build();
    }
}
