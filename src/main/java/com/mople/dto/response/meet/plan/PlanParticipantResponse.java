package com.mople.dto.response.meet.plan;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.plan.PlanParticipant;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanParticipantResponse(
        UserInfo user
) {
    public static List<PlanParticipantResponse> ofParticipantList(List<PlanParticipant> participants) {
        return participants.stream().map(PlanParticipantResponse::ofParticipant).toList();
    }

    private static PlanParticipantResponse ofParticipant(PlanParticipant participant) {
        return PlanParticipantResponse.builder()
                .user(UserInfo.from(participant.getUser()))
                .build();
    }
}