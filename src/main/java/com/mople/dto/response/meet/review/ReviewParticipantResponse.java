package com.mople.dto.response.meet.review;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.plan.PlanParticipant;
import lombok.Builder;

import java.util.List;

@Builder
public record ReviewParticipantResponse(
        UserInfo user
) {
    public static List<ReviewParticipantResponse> ofParticipantList(List<PlanParticipant> participants) {
        return participants.stream().map(ReviewParticipantResponse::ofParticipant).toList();
    }

    private static ReviewParticipantResponse ofParticipant(PlanParticipant participant) {
        return ReviewParticipantResponse.builder()
                .user(UserInfo.from(participant.getUser()))
                .build();
    }
}