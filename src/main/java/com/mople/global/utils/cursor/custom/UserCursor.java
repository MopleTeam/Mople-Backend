package com.mople.global.utils.cursor.custom;

import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.plan.PlanParticipant;

public record UserCursor(
        Integer roleOrder,
        Integer nicknameTypeOrder,
        String nicknameLower,
        Long id
) {

    public static UserCursor ofUserCursor(MeetMember member) {
        return new UserCursor(
                member.getRoleOrder(),
                member.getNicknameTypeOrder(),
                member.getNicknameLower(),
                member.getId()
        );
    }

    public static UserCursor ofUserCursor(PlanParticipant participant) {
        return new UserCursor(
                participant.getRoleOrder(),
                participant.getNicknameTypeOrder(),
                participant.getNicknameLower(),
                participant.getId()
        );
    }
}
