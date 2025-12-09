package com.mople.global.utils.cursor.custom;

import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.plan.PlanParticipant;

public record UserCursor(
        Integer deletedOrder,
        Integer roleOrder,
        Integer nicknameTypeOrder,
        String nicknameLower,
        Long id
) {

    public static UserCursor forMeetMember(MeetMember member) {
        return new UserCursor(
                0,
                member.getRoleOrder(),
                member.getNicknameTypeOrder(),
                member.getNicknameLower(),
                member.getId()
        );
    }

    public static UserCursor forPlan(PlanParticipant participant) {
        return new UserCursor(
                0,
                participant.getRoleOrder(),
                participant.getNicknameTypeOrder(),
                participant.getNicknameLower(),
                participant.getId()
        );
    }

    public static UserCursor forReview(PlanParticipant participant, int deletedOrder) {
        return new UserCursor(
                deletedOrder,
                participant.getRoleOrder(),
                participant.getNicknameTypeOrder(),
                participant.getNicknameLower(),
                participant.getId()
        );
    }
}
