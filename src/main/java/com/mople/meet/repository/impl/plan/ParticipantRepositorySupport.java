package com.mople.meet.repository.impl.plan;

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

    public List<PlanParticipant> findPlanParticipantPage(Long planId, Long creatorId, Long hostId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(participant.user, creatorId, hostId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(participant.user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(participant.user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.plan.id.eq(planId));

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
                .selectFrom(participant)
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

    public Long countPlanParticipants(Long planId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        Long count = queryFactory
                .select(participant.count())
                .from(participant)
                .where(participant.plan.id.eq(planId))
                .fetchOne();

        return count != null ? count : 0L;
    }

    public List<PlanParticipant> findReviewParticipantPage(Long reviewId, Long creatorId, Long hostId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        NumberExpression<Integer> roleOrder = MemberSortExpressions.roleOrder(participant.user, creatorId, hostId);
        NumberExpression<Integer> nicknameTypeOrder = MemberSortExpressions.nicknameTypeOrder(participant.user);
        StringExpression nicknameLower = MemberSortExpressions.nicknameLower(participant.user);

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.review.id.eq(reviewId));

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
                .selectFrom(participant)
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
                .where(participant.review.id.eq(reviewId))
                .fetchOne();

        return count != null ? count : 0L;
    }

    public boolean isCursorInvalid(String cursorNickname, Long cursorId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .selectOne()
                .from(participant)
                .where(participant.user.nickname.eq(cursorNickname), participant.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
