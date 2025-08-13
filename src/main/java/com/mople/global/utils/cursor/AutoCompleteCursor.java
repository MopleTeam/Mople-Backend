package com.mople.global.utils.cursor;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.Getter;

import static com.mople.global.utils.cursor.MemberSortExpressions.calculateRoleOrder;

@Getter
public class AutoCompleteCursor {

    private final int startsWithOrder;
    private final int roleOrder;
    private final String nicknameLower;
    private final Long id;

    public AutoCompleteCursor(String nickname, String keyword, Long userId, Long hostId, Long creatorId) {
        this.startsWithOrder = nickname.toLowerCase().startsWith(keyword.toLowerCase()) ? 1 : 2;
        this.roleOrder = calculateRoleOrder(userId, hostId, creatorId);
        this.nicknameLower = nickname.toLowerCase();
        this.id = userId;
    }

    public static BooleanExpression autoCompleteCursorCondition(
            NumberExpression<Integer> startsWithOrder,
            NumberExpression<Integer> roleOrder,
            StringExpression nicknameLower,
            NumberExpression<Long> idPath,
            AutoCompleteCursor cursor
    ) {
        BooleanExpression sameGroup = startsWithOrder.eq(cursor.getStartsWithOrder());

        return startsWithOrder.gt(cursor.getStartsWithOrder())
                .or(sameGroup.and(roleOrder.gt(cursor.getRoleOrder())))
                .or(sameGroup.and(roleOrder.eq(cursor.getRoleOrder()))
                        .and(nicknameLower.gt(cursor.getNicknameLower())))
                .or(sameGroup.and(roleOrder.eq(cursor.getRoleOrder()))
                        .and(nicknameLower.eq(cursor.getNicknameLower()))
                        .and(idPath.gt(cursor.getId())));
    }
}
