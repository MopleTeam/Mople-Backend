package com.mople.meet.repository.impl.plan;

import com.mople.dto.response.meet.PlanPageResponse;
import com.mople.dto.response.meet.ReviewPageResponse;
import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.dto.response.meet.plan.PlanListResponse;
import com.mople.dto.response.meet.plan.PlanViewResponse;
import com.mople.entity.meet.*;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.QMeetPlan;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.meet.review.QPlanReview;
import com.mople.global.enums.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlanRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanViewResponse> findHomeViewPlan(Long userId, int size) {
        QMeetPlan plan = QMeetPlan.meetPlan;
        QMeet meet = QMeet.meet;
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QPlanParticipant ppAll = new QPlanParticipant("ppAll");

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanViewResponse.class,
                                plan.id,
                                plan.version,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.name,
                                plan.creatorId,
                                JPAExpressions
                                        .select(ppAll.count().intValue())
                                        .from(ppAll)
                                        .where(ppAll.planId.eq(plan.id)),
                                plan.planTime,
                                plan.address,
                                plan.title,
                                plan.latitude,
                                plan.longitude,
                                plan.weatherIcon,
                                plan.weatherAddress,
                                plan.temperature,
                                plan.pop
                        )
                )
                .from(plan)
                .join(participant).on(
                        participant.planId.eq(plan.id)
                                .and(participant.userId.eq(userId))
                )
                .join(meet).on(meet.id.eq(plan.meetId))
                .where(
                        plan.status.eq(Status.ACTIVE),
                        meet.status.eq(Status.ACTIVE),
                        plan.planTime.after(
                                Expressions.dateTimeOperation(
                                        LocalDateTime.class, Ops.DateTimeOps.CURRENT_TIMESTAMP
                                )
                        )
                )
                .orderBy(plan.planTime.asc())
                .limit(size)
                .fetch();
    }

    public List<PlanListResponse> findPlanPage(Long userId, Long meetId, Long cursorId, int size) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QPlanParticipant ppAll = new QPlanParticipant("ppAll");

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(plan.status.eq(Status.ACTIVE))
                .and(meet.status.eq(Status.ACTIVE))
                .and(plan.meetId.eq(meetId))
                .and(plan.planTime.after(
                        Expressions.dateTimeOperation(
                                LocalDateTime.class, Ops.DateTimeOps.CURRENT_DATE))
                );

        if (cursorId != null) {
            LocalDateTime cursorPlanTime = queryFactory
                    .select(plan.planTime)
                    .from(plan)
                    .where(plan.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(plan.planTime.gt(cursorPlanTime)
                    .or(plan.planTime.eq(cursorPlanTime).and(plan.id.gt(cursorId)))
            );
        }

        BooleanExpression joinedByUser = JPAExpressions
                .selectOne()
                .from(participant)
                .where(
                        participant.planId.eq(plan.id)
                                .and(participant.userId.eq(userId))
                )
                .exists();

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanListResponse.class,
                                plan.id,
                                plan.version,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.name,
                                JPAExpressions
                                        .select(ppAll.count().intValue())
                                        .from(ppAll)
                                        .where(ppAll.planId.eq(plan.id)),
                                plan.planTime,
                                plan.address,
                                plan.title,
                                plan.creatorId,
                                plan.weatherIcon,
                                plan.weatherAddress,
                                plan.temperature,
                                plan.pop,
                                joinedByUser
                        )
                )
                .from(plan)
                .join(meet).on(meet.id.eq(plan.meetId))
                .where(whereCondition)
                .orderBy(plan.planTime.asc(), plan.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QMeetPlan plan = QMeetPlan.meetPlan;

        return queryFactory
                .selectOne()
                .from(plan)
                .where(
                        plan.status.eq(Status.ACTIVE),
                        plan.id.eq(cursorId)
                )
                .fetchFirst() == null;
    }

    public UserPageResponse getPlanAndReviewPages(Long userId, YearMonth date) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;
        QMeetMember meetMember = QMeetMember.meetMember;
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QPlanParticipant ppAll = new QPlanParticipant("ppAll");

        LocalDateTime start = date.atDay(1).atTime(0, 0, 0);
        LocalDateTime end = date.atEndOfMonth().atTime(23, 59, 59);

        List<PlanPageResponse> plans = queryFactory
                .select(
                        Projections.constructor(
                                PlanPageResponse.class,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.id,
                                plan.name,
                                plan.planTime,
                                JPAExpressions
                                        .select(ppAll.count().intValue())
                                        .from(ppAll)
                                        .where(ppAll.planId.eq(plan.id)),
                                plan.weatherIcon,
                                plan.weatherAddress,
                                plan.temperature,
                                plan.pop
                        )
                )
                .from(meet)
                .join(meet).on(meet.id.eq(plan.meetId))
                .join(meetMember).on(
                        meetMember.meetId.eq(meet.id)
                                .and(meetMember.userId.eq(userId))
                )
                .join(participant).on(
                        participant.planId.eq(plan.id)
                                .and(participant.userId.eq(userId))
                )
                .where(
                        plan.status.eq(Status.ACTIVE),
                        meet.status.eq(Status.ACTIVE),
                        getBetweenDate(plan.planTime, start, end)
                )
                .fetch();

        List<ReviewPageResponse> reviews = queryFactory
                .select(
                        Projections.constructor(
                                ReviewPageResponse.class,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                review.id,
                                review.name,
                                review.planTime,
                                JPAExpressions
                                        .select(ppAll.count().intValue())
                                        .from(ppAll)
                                        .where(ppAll.reviewId.eq(review.id)),
                                review.weatherIcon,
                                review.weatherAddress,
                                review.temperature,
                                review.pop
                        )
                )
                .from(meet)
                .join(meet).on(meet.id.eq(review.meetId))
                .join(meetMember).on(
                        meetMember.meetId.eq(meet.id)
                                .and(meetMember.userId.eq(userId))
                )
                .join(participant).on(
                        participant.reviewId.eq(review.id)
                                .and(participant.userId.eq(userId))
                )
                .where(
                        review.status.eq(Status.ACTIVE),
                        meet.status.eq(Status.ACTIVE),
                        getBetweenDate(review.planTime, start, end)
                )
                .fetch();

        return new UserPageResponse(reviews, plans);
    }

    public UserAllDateResponse getAllDate(Long userId) {
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;
        QMeetMember member = QMeetMember.meetMember;
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanExpression isMemberForPlan = JPAExpressions
                .selectOne()
                .from(member)
                .where(member.meetId.eq(plan.meetId)
                        .and(member.userId.eq(userId)))
                .exists();

        BooleanExpression isParticipantForPlan = JPAExpressions
                .selectOne()
                .from(participant)
                .where(participant.planId.eq(plan.id)
                        .and(participant.userId.eq(userId)))
                .exists();

        List<LocalDateTime> planDate = queryFactory
                .select(plan.planTime)
                .from(plan)
                .where(
                        plan.status.eq(Status.ACTIVE),
                        isMemberForPlan.and(isParticipantForPlan)
                )
                .distinct()
                .fetch();

        BooleanExpression isMemberForReview = JPAExpressions
                .selectOne()
                .from(member)
                .where(member.meetId.eq(review.meetId)
                        .and(member.userId.eq(userId)))
                .exists();

        BooleanExpression isParticipantForReview = JPAExpressions
                .selectOne()
                .from(participant)
                .where(participant.reviewId.eq(review.id)
                        .and(participant.userId.eq(userId)))
                .exists();

        List<LocalDateTime> reviewDate = queryFactory
                .select(review.planTime)
                .from(review)
                .where(
                        review.status.eq(Status.ACTIVE),
                        isMemberForReview.and(isParticipantForReview)
                )
                .distinct()
                .fetch();

        return new UserAllDateResponse(planDate, reviewDate);
    }

    private BooleanExpression getBetweenDate(DateTimePath<LocalDateTime> planTime, LocalDateTime start, LocalDateTime end) {
        return planTime.between(start, end);
    }

    public List<Long> findUpdateWeatherPlan() {
        QMeetPlan plan = QMeetPlan.meetPlan;
        var now = LocalDateTime.now();

        return queryFactory
                .select(plan.id)
                .from(plan)
                .where(
                        plan.status.eq(Status.ACTIVE),
                        plan.planTime.before(now.plusDays(5))
                )
                .orderBy(
                        new CaseBuilder()
                                .when(plan.weatherUpdatedAt.isNull())
                                .then(1)
                                .otherwise(2)
                                .asc()
                )
                .limit(40)
                .fetch();
    }
}
