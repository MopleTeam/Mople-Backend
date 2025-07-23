package com.mople.meet.repository.impl.plan;

import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParticipantRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanParticipant> findParticipantFirstPage(Long planId, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .selectFrom(participant)
                .where(participant.plan.id.eq(planId))
                .orderBy(participant.user.nickname.asc(), participant.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<PlanParticipant> findParticipantNextPage(Long planId, String cursorNickname, Long cursorId, int size) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .selectFrom(participant)
                .where(
                        participant.plan.id.eq(planId),
                        participant.user.nickname.gt(cursorNickname)
                                .or(participant.user.nickname.eq(cursorNickname)
                                        .and(participant.id.gt(cursorId)))
                )
                .orderBy(participant.user.nickname.asc(), participant.id.asc())
                .limit(size + 1)
                .fetch();
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
