package com.mople.dto.response.meet.review;

import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.review.PlanReview;

import java.util.List;

public record ReviewParticipantResponse(
        Long creatorId,
        List<ReviewParticipant> members
) {
    public ReviewParticipantResponse(PlanReview review) {
        this(
                review.getCreatorId(),
                review.getParticipants().stream().map(ReviewParticipant::new).toList()
        );
    }

    public record ReviewParticipant(
            Long memberId,
            String nickname,
            String profileImg
    ) {
        public ReviewParticipant(PlanParticipant participant) {
            this(
                    participant.getUser().getId(),
                    participant.getUser().getNickname(),
                    participant.getUser().getProfileImg()
            );
        }

    }
}
