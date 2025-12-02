package com.mople.global.utils.cursor.custom.sort;

import com.mople.entity.meet.QMeetMember;
import com.mople.entity.user.QUser;
import com.mople.global.enums.Status;
import com.mople.global.enums.UserRole;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;

public class MemberSortExpressions {

    public static NumberExpression<Integer> startsWithOrder(QMeetMember member, String keyword) {
        return new CaseBuilder()
                .when(member.nicknameLower.startsWithIgnoreCase(keyword)).then(1)
                .otherwise(2);
    }

    public static Integer startsWithOrder(String nickname, String keyword) {
        if (nickname.startsWith(keyword)) {
            return 1;
        }

        return 2;
    }

    public static NumberExpression<Integer> deletedOrder(QUser user) {
        return new CaseBuilder()
                        .when(user.status.eq(Status.DELETED)).then(1)
                        .otherwise(0);
    }

    public static Integer deletedOrder(Status status) {
        if (status.equals(Status.DELETED)) {
            return 1;
        }

        return 0;
    }

    public static Integer calculateRoleOrder(Long userId, Long hostId, Long creatorId) {
        if (userId.equals(hostId)) {
            return UserRole.HOST.getOrder();
        } else if (userId.equals(creatorId)) {
            return UserRole.CREATOR.getOrder();
        } else {
            return UserRole.PARTICIPANT.getOrder();
        }
    }

    public static Integer calculateNicknameTypeOrder(String nickname) {
        String firstChar = nickname.substring(0, 1);
        if (firstChar.matches("[가-힣]")) {
            return 1;
        } else if (firstChar.matches("[A-Za-z]")) {
            return 2;
        } else if (firstChar.matches("[0-9]")) {
            return 3;
        } else {
            return 4;
        }
    }
}
