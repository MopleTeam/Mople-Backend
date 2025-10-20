package com.mople.meet.repository.impl.plan;

import com.mople.global.utils.cursor.custom.MemberCursor;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mople.global.utils.cursor.custom.MemberCursor.memberCursorCondition;

@Repository
@RequiredArgsConstructor
public class ParticipantRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanParticipant> findPlanParticipantPage(Long planId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.planId.eq(planId));

        if (cursor != null) {
            whereCondition.and(
                    memberCursorCondition(
                            participant.roleOrder,
                            participant.nicknameTypeOrder,
                            participant.nicknameLower,
                            participant.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .select(participant)
                .from(participant)
                .where(whereCondition)
                .orderBy(
                        participant.roleOrder.asc(),
                        participant.nicknameTypeOrder.asc().nullsLast(),
                        participant.nicknameLower.asc(),
                        participant.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }

    public List<PlanParticipant> findReviewParticipantPage(Long reviewId, MemberCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.reviewId.eq(reviewId));

        if (cursor != null) {
            whereCondition.and(
                    memberCursorCondition(
                            participant.roleOrder,
                            participant.nicknameTypeOrder,
                            participant.nicknameLower,
                            participant.id,
                            cursor
                    )
            );
        }

        return queryFactory
                .select(participant)
                .from(participant)
                .where(whereCondition)
                .orderBy(
                        participant.roleOrder.asc(),
                        participant.nicknameTypeOrder.asc().nullsLast(),
                        participant.nicknameLower.asc(),
                        participant.id.asc()
                )
                .limit(size + 1)
                .fetch();
    }
}
