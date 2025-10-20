package com.mople.global.utils.cursor.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

public record MemberCursor(
        Integer roleOrder,
        Integer nicknameTypeOrder,
        String nicknameLower,
        Long id
) {

    public static BooleanExpression memberCursorCondition(
            NumberExpression<Integer> roleOrder,
            NumberExpression<Integer> nicknameTypeOrder,
            StringExpression nicknameLower,
            NumberExpression<Long> idPath,
            MemberCursor cursor
    ) {

        return roleOrder.gt(cursor.roleOrder())
                .or(roleOrder.eq(cursor.roleOrder())
                        .and(nicknameTypeOrder.gt(cursor.nicknameTypeOrder())))
                .or(roleOrder.eq(cursor.roleOrder())
                        .and(nicknameTypeOrder.eq(cursor.nicknameTypeOrder()))
                        .and(nicknameLower.gt(cursor.nicknameLower())))
                .or(roleOrder.eq(cursor.roleOrder())
                        .and(nicknameTypeOrder.eq(cursor.nicknameTypeOrder()))
                        .and(nicknameLower.eq(cursor.nicknameLower()))
                        .and(idPath.gt(cursor.id())));
    }
}
