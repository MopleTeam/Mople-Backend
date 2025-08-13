package com.mople.global.utils.cursor;

import com.mople.entity.user.QUser;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

public class MemberSortExpressions {

    public static NumberExpression<Integer> startsWithOrder(QUser user, String keyword) {
        return new CaseBuilder()
                .when(user.nickname.startsWithIgnoreCase(keyword)).then(1)
                .otherwise(2);
    }

    public static NumberExpression<Integer> roleOrder(QUser user, Long hostId) {
        return new CaseBuilder()
                .when(user.id.eq(hostId)).then(1)
                .otherwise(2);
    }

    public static NumberExpression<Integer> roleOrder(QUser user, Long hostId, Long creatorId) {
        return new CaseBuilder()
                .when(user.id.eq(hostId)).then(1)
                .when(user.id.eq(creatorId)).then(2)
                .otherwise(3);
    }

    public static NumberExpression<Integer> nicknameTypeOrder(QUser user) {
        StringExpression firstChar = user.nickname.substring(0, 1);

        return new CaseBuilder()
                .when(firstChar.between("가", "힣")).then(1)
                .when(firstChar.between("A", "z")).then(2)
                .when(firstChar.between("0", "9")).then(3)
                .otherwise(4);
    }

    public static StringExpression nicknameLower(QUser user) {
        return user.nickname.lower();
    }

    public static int calculateRoleOrder(Long userId, Long hostId) {
        if (userId.equals(hostId)) {
            return 1;
        }  else {
            return 2;
        }
    }

    public static int calculateRoleOrder(Long userId, Long hostId, Long creatorId) {
        if (userId.equals(hostId)) {
            return 1;
        } else if (userId.equals(creatorId)) {
            return 2;
        } else {
            return 3;
        }
    }

    public static int calculateNicknameTypeOrder(String nickname) {
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
