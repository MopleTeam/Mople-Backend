package com.mople.meet.repository.impl.plan;

import com.mople.global.utils.cursor.custom.UserCursor;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ParticipantRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanParticipant> findPlanParticipantPage(Long planId, UserCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.planId.eq(planId));

        if (cursor != null) {
            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1}, {2}, {3} ) > ( {4}, {5}, {6}, {7} )",
                            participant.roleOrder, participant.nicknameTypeOrder, participant.nicknameLower, participant.id,
                            cursor.roleOrder(), cursor.nicknameTypeOrder(), cursor.nicknameLower(), cursor.id()
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

    public List<PlanParticipant> findReviewParticipantPage(Long reviewId, UserCursor cursor, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(participant.reviewId.eq(reviewId));

        if (cursor != null) {
            whereCondition.and(
                    Expressions.booleanTemplate(
                            "( {0}, {1}, {2}, {3} ) > ({4}, {5}, {6}, {7})",
                            participant.roleOrder, participant.nicknameTypeOrder, participant.nicknameLower, participant.id,
                            cursor.roleOrder(), cursor.nicknameTypeOrder(), cursor.nicknameLower(), cursor.id()
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

    public Map<Long, Integer> reviewParticipantCountMap(List<Long> reviewIds) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

         return queryFactory
                .select(participant.reviewId, participant.count())
                .from(participant)
                .where(participant.reviewId.in(reviewIds))
                .groupBy(participant.reviewId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(participant.reviewId),
                        tuple -> Objects.requireNonNull(tuple.get(participant.count())).intValue()
                        )
                );
    }

    public Map<Long, Integer> planParticipantCountMap(List<Long> planIds) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

         return queryFactory
                .select(participant.planId, participant.count())
                .from(participant)
                .where(participant.planId.in(planIds))
                .groupBy(participant.planId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(participant.planId),
                        tuple -> Objects.requireNonNull(tuple.get(participant.count())).intValue()
                        )
                );
    }

    public Set<Long> findJoinedPlanIds(Long userId, List<Long> planIds) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return new HashSet<>(
                queryFactory
                        .select(participant.planId)
                        .from(participant)
                        .where(
                                participant.planId.in(planIds),
                                participant.userId.eq(userId)
                        )
                        .fetch()
        );
    }
}
