package com.mople.dto.client;

import com.mople.dto.response.meet.review.ReviewParticipantResponse;
import com.mople.dto.response.pagination.CursorPageResponse;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReviewParticipantClientResponse {
    private final Long creatorId;
    private final CursorPageResponse<ReviewParticipantResponse> members;

    public static ReviewParticipantClientResponse ofParticipants(Long creatorId, CursorPageResponse<ReviewParticipantResponse> members) {
        return ReviewParticipantClientResponse.builder()
                .creatorId(creatorId)
                .members(members)
                .build();
    }
}
