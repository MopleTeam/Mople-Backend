package com.mople.meet.repository.impl;

import com.mople.entity.user.QUser;
import com.mople.global.utils.cursor.AutoCompleteCursor;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.QMeetMember;
import com.mople.global.utils.cursor.MemberSortExpressions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetMemberRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<MeetMember> findMemberPage(Long meetId, Long hostId, MemberCursor cursor, int size) {
        QMeetMember member = QMeetMember.meetMember;
        QUser user = QUser.user;

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(user, hostId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.meetId.eq(meetId));

        if (cursor != null) {
            whereCondition.and(
                    MemberCursor.memberCursorCondition(
                            roleOrder,
                            nicknameTypeOrder,
                            nicknameLower,
                            member.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .selectFrom(member)
                .where(whereCondition)
                .join(user).on(user.id.eq(member.userId))
                .orderBy(
                        roleOrder.asc(),
                        nicknameTypeOrder.asc().nullsLast(),
                        nicknameLower.asc(),
                        member.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public List<MeetMember> findMemberAutoCompletePage(
            Long meetId,
            Long hostId,
            Long creatorId,
            String keyword,
            AutoCompleteCursor cursor,
            int size
    ) {
        QMeetMember member = QMeetMember.meetMember;
        QUser user = QUser.user;

        NumberExpression<Integer> startsWithOrder = MemberSortExpressions.startsWithOrder(user, keyword);
        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(user, hostId, creatorId);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.meetId.eq(meetId))
                .and(user.nickname.containsIgnoreCase(keyword));

        if (cursor != null) {
            whereCondition.and(
                    AutoCompleteCursor.autoCompleteCursorCondition(
                            startsWithOrder,
                            roleOrder,
                            nicknameLower,
                            member.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .selectFrom(member)
                .join(user).on(user.id.eq(member.userId))
                .where(whereCondition)
                .orderBy(
                        startsWithOrder.asc(),
                        roleOrder.asc(),
                        nicknameLower.asc(),
                        member.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public boolean isCursorInvalid(String cursorNickname, Long cursorId) {
        QMeetMember member = QMeetMember.meetMember;
        QUser user = QUser.user;

        return queryFactory
                .selectOne()
                .from(member)
                .join(user).on(user.id.eq(member.userId))
                .where(user.nickname.eq(cursorNickname), member.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
