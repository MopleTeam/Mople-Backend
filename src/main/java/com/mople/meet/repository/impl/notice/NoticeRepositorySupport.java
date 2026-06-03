package com.mople.meet.repository.impl.notice;

import com.mople.entity.meet.notice.MeetNotice;
import com.mople.entity.meet.notice.QMeetNotice;
import com.mople.global.enums.notice.NoticeType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoticeRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public List<MeetNotice> findNoticePage(Long meetId, NoticeType type, Long cursorId, int size) {
        QMeetNotice notice = QMeetNotice.meetNotice;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(notice.meetId.eq(meetId));

        if (type != null) {
            whereCondition.and(notice.type.eq(type));
        }

        if (cursorId != null) {
            LocalDateTime cursorWriteTime = queryFactory
                    .select(notice.createdAt)
                    .from(notice)
                    .where(notice.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1} ) < ( {2}, {3} )",
                            notice.createdAt, notice.id, cursorWriteTime, cursorId
                    )
            );
        }

        return queryFactory
                .selectFrom(notice)
                .where(whereCondition)
                .orderBy(notice.createdAt.desc(), notice.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QMeetNotice notice = QMeetNotice.meetNotice;

        return queryFactory
                .selectOne()
                .from(notice)
                .where(
                        notice.id.eq(cursorId)
                )
                .fetchFirst() == null;
    }
}
