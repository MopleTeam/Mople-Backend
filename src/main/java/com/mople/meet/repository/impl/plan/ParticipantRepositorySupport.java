package com.mople.meet.repository.impl.plan;

import com.mople.entity.user.QUser;
import com.mople.global.utils.cursor.MemberCursor;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.global.utils.cursor.MemberSortExpressions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mople.global.utils.cursor.MemberCursor.memberCursorCondition;

@Repository
@RequiredArgsConstructor
public class ParticipantRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanParticipant> findPlanParticipantPage(Long planId, Long hostId, Long creatorId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QUser user = QUser.user;

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(user, hostId, creatorId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.planId.eq(planId));

        if (cursor != null) {
            whereCondition.and(
                    memberCursorCondition(
                            roleOrder,
                            nicknameTypeOrder,
                            nicknameLower,
                            participant.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .select(participant)
                .from(participant)
                .join(user).on(user.id.eq(participant.userId))
                .where(whereCondition)
                .orderBy(
                        roleOrder.asc(),
                        nicknameTypeOrder.asc().nullsLast(),
                        nicknameLower.asc(),
                        participant.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public List<PlanParticipant> findReviewParticipantPage(Long reviewId, Long hostId, Long creatorId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QUser user = QUser.user;

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(user, hostId, creatorId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.reviewId.eq(reviewId));

        if (cursor != null) {
            whereCondition.and(
                    memberCursorCondition(
                            roleOrder,
                            nicknameTypeOrder,
                            nicknameLower,
                            participant.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .select(participant)
                .from(participant)
                .join(user).on(user.id.eq(participant.userId))
                .where(whereCondition)
                .orderBy(
                        roleOrder.asc(),
                        nicknameTypeOrder.asc().nullsLast(),
                        nicknameLower.asc(),
                        participant.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public Long countReviewParticipants(Long reviewId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        Long count = queryFactory
                .select(participant.count())
                .from(participant)
                .where(participant.reviewId.eq(reviewId))
                .fetchOne();

        return count != null ? count : 0L;
    }

    public boolean isCursorInvalid(String cursorNickname, Long cursorId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QUser user = QUser.user;

        return queryFactory
                .selectOne()
                .from(participant)
                .join(user).on(user.id.eq(participant.userId))
                .where(user.nickname.eq(cursorNickname), participant.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
