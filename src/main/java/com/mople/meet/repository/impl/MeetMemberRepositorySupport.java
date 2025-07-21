package com.mople.meet.repository.impl;

import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.QMeetMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetMemberRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<MeetMember> findMemberFirstPage(Long meetId, String keyword, int size) {
        QMeetMember member = QMeetMember.meetMember;

        return queryFactory
                .selectFrom(member)
                .where(
                        member.joinMeet.id.eq(meetId),
                        member.user.nickname.containsIgnoreCase(keyword)
                )
                .orderBy(member.user.nickname.asc(), member.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<MeetMember> findMemberNextPage(Long meetId, String keyword, String cursorNickname, Long cursorId, int size) {
        QMeetMember member = QMeetMember.meetMember;

        return queryFactory
                .selectFrom(member)
                .where(
                        member.joinMeet.id.eq(meetId),
                        member.user.nickname.containsIgnoreCase(keyword),
                        member.user.nickname.gt(cursorNickname)
                                .or(member.user.nickname.eq(cursorNickname)
                                        .and(member.id.gt(cursorId)))
                )
                .orderBy(member.user.nickname.asc(), member.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public boolean validateCursor(String cursorNickname, Long cursorId) {
        QMeetMember member = QMeetMember.meetMember;

        return queryFactory
                .selectOne()
                .from(member)
                .where(member.user.nickname.eq(cursorNickname), member.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
