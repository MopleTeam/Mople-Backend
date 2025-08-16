package com.mople.meet.repository.impl;

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

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(member.user, hostId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(member.user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(member.user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.joinMeet.id.eq(meetId));

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
                .orderBy(
                        roleOrder.asc(),
                        nicknameTypeOrder.asc().nullsLast(),
                        nicknameLower.asc(),
                        member.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public Long countMeetMembers(Long meetId) {
        QMeetMember member = QMeetMember.meetMember;

        Long count = queryFactory
                .select(member.count())
                .from(member)
                .where(member.joinMeet.id.eq(meetId))
                .fetchOne();

        return count != null ? count : 0L;
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

        NumberExpression<Integer> startsWithOrder = MemberSortExpressions.startsWithOrder(member.user, keyword);
        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(member.user, hostId, creatorId);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(member.user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(member.joinMeet.id.eq(meetId))
                .and(member.user.nickname.containsIgnoreCase(keyword));

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

        return queryFactory
                .selectOne()
                .from(member)
                .where(member.user.nickname.eq(cursorNickname), member.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
