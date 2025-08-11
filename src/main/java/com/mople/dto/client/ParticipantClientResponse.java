package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.global.enums.UserRole;
import lombok.Builder;

import java.util.List;

@Builder
public record ParticipantClientResponse(
        UserInfo user
) {
    public static List<ParticipantClientResponse> ofParticipantList(List<PlanParticipant> participants, Long creatorId, Long hostId) {
        return participants.stream()
                .map(p -> ofParticipant(p, UserRole.getRole(p.getId(), creatorId, hostId)))
                .toList();
    }

    private static ParticipantClientResponse ofParticipant(PlanParticipant participant, UserRole role) {
        return ParticipantClientResponse.builder()
                .user(UserInfo.from(participant.getUser(), role))
                .build();
    }
}