package com.mople.meet.repository.impl;

import com.mople.global.utils.cursor.custom.AutoCompleteCursor;
import com.mople.global.utils.cursor.custom.UserCursor;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.QMeetMember;
import com.mople.global.utils.cursor.custom.sort.MemberSortExpressions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetMemberRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<MeetMember> findMemberPage(Long meetId, UserCursor cursor, int size) {
        QMeetMember member = QMeetMember.meetMember;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.meetId.eq(meetId));

        if (cursor != null) {
            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1}, {2}, {3} ) > ( {4}, {5}, {6}, {7} )",
                            member.roleOrder, member.nicknameTypeOrder, member.nicknameLower, member.id,
                            cursor.roleOrder(), cursor.nicknameTypeOrder(), cursor.nicknameLower(), cursor.id()
                    )
            );
        }

        return queryFactory
                .selectFrom(member)
                .where(whereCondition)
                .orderBy(
                        member.roleOrder.asc(),
                        member.nicknameTypeOrder.asc().nullsLast(),
                        member.nicknameLower.asc(),
                        member.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public List<MeetMember> findMemberAutoCompletePage(
            Long meetId,
            String keyword,
            AutoCompleteCursor cursor,
            int size
    ) {
        QMeetMember member = QMeetMember.meetMember;

        NumberExpression<Integer> startsWithOrder = MemberSortExpressions.startsWithOrder(member, keyword);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.meetId.eq(meetId))
                .and(member.nicknameLower.contains(keyword));

        if (cursor != null) {
            int cursorStartsWithOrder = MemberSortExpressions.startsWithOrder(cursor.nicknameLower(), keyword);

            whereCondition.and(
                    AutoCompleteCursor.autoCompleteCursorCondition(
                            startsWithOrder,
                            member.roleOrder,
                            member.nicknameLower,
                            member.id,
                            cursorStartsWithOrder,
                            cursor
                    )
            );
        }

        return queryFactory
                .selectFrom(member)
                .where(whereCondition)
                .orderBy(
                        startsWithOrder.asc(),
                        member.roleOrder.asc(),
                        member.nicknameLower.asc(),
                        member.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }
}
