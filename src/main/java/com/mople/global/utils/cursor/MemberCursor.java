package com.mople.global.utils.cursor;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.Getter;

import static com.mople.global.utils.cursor.MemberSortExpressions.calculateNicknameTypeOrder;
import static com.mople.global.utils.cursor.MemberSortExpressions.calculateRoleOrder;

@Getter
public class MemberCursor {

    private final int roleOrder;
    private final int nicknameTypeOrder;
    private final String nicknameLower;
    private final Long id;

    public MemberCursor(String nickname, Long userId, Long hostId) {
        this.roleOrder = calculateRoleOrder(userId, hostId);
        this.nicknameTypeOrder = calculateNicknameTypeOrder(nickname);
        this.nicknameLower = nickname.toLowerCase();
        this.id = userId;
    }

    public MemberCursor(String nickname, Long userId, Long hostId, Long creatorId) {
        this.roleOrder = calculateRoleOrder(userId, hostId, creatorId);
        this.nicknameTypeOrder = calculateNicknameTypeOrder(nickname);
        this.nicknameLower = nickname.toLowerCase();
        this.id = userId;
    }

    public static BooleanExpression memberCursorCondition(
            NumberExpression<Integer> roleOrder,
            NumberExpression<Integer> nicknameTypeOrder,
            StringExpression nicknameLower,
            NumberExpression<Long> idPath,
            MemberCursor cursor
    ) {

        return roleOrder.gt(cursor.getRoleOrder())
                .or(roleOrder.eq(cursor.getRoleOrder())
                        .and(nicknameTypeOrder.gt(cursor.getNicknameTypeOrder())))
                .or(roleOrder.eq(cursor.getRoleOrder())
                        .and(nicknameTypeOrder.eq(cursor.getNicknameTypeOrder()))
                        .and(nicknameLower.gt(cursor.getNicknameLower())))
                .or(roleOrder.eq(cursor.getRoleOrder())
                        .and(nicknameTypeOrder.eq(cursor.getNicknameTypeOrder()))
                        .and(nicknameLower.eq(cursor.getNicknameLower()))
                        .and(idPath.gt(cursor.getId())));
    }
}
