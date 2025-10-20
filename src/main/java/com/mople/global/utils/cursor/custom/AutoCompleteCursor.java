package com.mople.global.utils.cursor.custom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

public record AutoCompleteCursor(
        Integer roleOrder,
        String nicknameLower,
        Long id
) {

    public static BooleanExpression autoCompleteCursorCondition(
            NumberExpression<Integer> startsWithOrder,
            NumberExpression<Integer> roleOrder,
            StringExpression nicknameLower,
            NumberExpression<Long> idPath,
            int cursorStartsWithOrder,
            AutoCompleteCursor cursor
    ) {
        BooleanExpression sameGroup = startsWithOrder.eq(cursorStartsWithOrder);

        return startsWithOrder.gt(cursorStartsWithOrder)
                .or(sameGroup.and(roleOrder.gt(cursor.roleOrder())))
                .or(sameGroup.and(roleOrder.eq(cursor.roleOrder()))
                        .and(nicknameLower.gt(cursor.nicknameLower())))
                .or(sameGroup.and(roleOrder.eq(cursor.roleOrder()))
                        .and(nicknameLower.eq(cursor.nicknameLower()))
                        .and(idPath.gt(cursor.id())));
    }
}
