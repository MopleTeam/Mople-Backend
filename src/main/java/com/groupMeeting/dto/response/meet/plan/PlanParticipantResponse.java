package com.groupMeeting.dto.response.meet.plan;

import com.groupMeeting.entity.meet.plan.MeetPlan;
import com.groupMeeting.entity.meet.plan.PlanParticipant;

import java.util.List;

public record PlanParticipantResponse(
        Long creatorId,
        List<PlanParticipantListResponse> members
) {
    public PlanParticipantResponse(MeetPlan plan) {
        this(plan.getCreator().getId(), plan.getParticipants().stream().map(PlanParticipantListResponse::new).toList());
    }

    public record PlanParticipantListResponse(
            Long memberId,
            String nickname,
            String profileImg
    ) {
        public PlanParticipantListResponse(PlanParticipant participant) {
            this(participant.getUser().getId(), participant.getUser().getNickname(), participant.getUser().getProfileImg());
        }
    }
}
