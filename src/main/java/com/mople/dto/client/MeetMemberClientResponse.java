package com.mople.dto.client;

import com.mople.dto.response.meet.MeetMemberResponse;
import com.mople.dto.response.pagination.CursorPageResponse;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MeetMemberClientResponse {
    private final Long creatorId;
    private final CursorPageResponse<MeetMemberResponse> members;

    public static MeetMemberClientResponse ofMembers(Long creatorId, CursorPageResponse<MeetMemberResponse> members) {
        return MeetMemberClientResponse.builder()
                .creatorId(creatorId)
                .members(members)
                .build();

    }
}