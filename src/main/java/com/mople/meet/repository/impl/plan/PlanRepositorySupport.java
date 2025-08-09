package com.mople.meet.repository.impl.plan;

import com.mople.dto.response.meet.PlanPageResponse;
import com.mople.dto.response.meet.ReviewPageResponse;
import com.mople.dto.response.meet.UserAllDateResponse;
import com.mople.dto.response.meet.UserPageResponse;
import com.mople.dto.response.meet.plan.PlanDetailResponse;
import com.mople.dto.response.meet.plan.PlanListResponse;
import com.mople.dto.response.meet.plan.PlanViewResponse;
import com.mople.entity.meet.*;
import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.QMeetPlan;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.meet.review.QPlanReview;
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

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanViewResponse.class,
                                plan.id,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.name,
                                plan.creator.id,
                                plan.participants.size(),
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
                .join(plan.participants, participant)
                .on(participant.user.id.eq(userId))
                .join(plan.meet, meet)
                .on(meet.id.eq(plan.meet.id))
                .where(plan.planTime.after(
                        Expressions.dateTimeOperation(
                                LocalDateTime.class, Ops.DateTimeOps.CURRENT_TIMESTAMP))
                )
                .orderBy(plan.planTime.asc())
                .limit(size)
                .fetch();
    }

    public PlanDetailResponse findPlanDetail(Long planId) {
        QMeetPlan plan = QMeetPlan.meetPlan;
        QMeet meet = QMeet.meet;

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanDetailResponse.class,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.id,
                                plan.name,
                                plan.planTime,
                                plan.address,
                                plan.participants.size()
                        )
                )
                .from(plan)
                .join(plan.meet, meet)
                .on(plan.meet.id.eq(meet.id))
                .fetchOne();
    }

    public List<PlanListResponse> findPlanPage(Long userId, Long meetId, Long cursorId, int size) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(plan.meet.id.eq(meetId))
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

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanListResponse.class,
                                plan.id,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.name,
                                plan.participants.size(),
                                plan.planTime,
                                plan.address,
                                plan.title,
                                plan.creator.id,
                                plan.weatherIcon,
                                plan.weatherAddress,
                                plan.temperature,
                                plan.pop,
                                plan.participants.contains(
                                        JPAExpressions
                                                .selectFrom(participant)
                                                .where(plan.id.eq(participant.plan.id).and(participant.user.id.eq(userId)))
                                )
                        )
                )
                .from(plan)
                .join(plan.meet, meet)
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
                .where(plan.id.eq(cursorId))
                .fetchFirst() == null;
    }

    public List<PlanListResponse> findPreviousPlanList(Long userId) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QMeetMember meetMember = QMeetMember.meetMember;
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QPlanReview review = QPlanReview.planReview;

        return queryFactory
                .select(
                        Projections.constructor(
                                PlanListResponse.class,
                                plan.id,
                                meet.id,
                                meet.name,
                                meet.meetImage,
                                plan.name,
                                plan.participants.size(),
                                plan.planTime,
                                plan.address,
                                plan.weatherIcon,
                                plan.temperature,
                                Expressions.TRUE
                        )
                )
                .join(meet, plan.meet)
                .on(meet.id.eq(plan.meet.id))
                .join(meet, review.meet)
                .on(meet.id.eq(review.meet.id))
                .join(meet.members, meetMember)
                .on(meetMember.user.id.eq(userId))
                .where(
                        plan.planTime.before(
                                Expressions.dateTimeOperation(
                                        LocalDateTime.class, Ops.DateTimeOps.CURRENT_TIMESTAMP)
                        ).and(
                                plan.participants.contains(
                                        JPAExpressions
                                                .selectFrom(participant)
                                                .where(participant.user.id.eq(userId))
                                )
                        )
                )
                .orderBy(plan.createdAt.desc())
                .fetch();
    }

    public UserPageResponse getPlanAndReviewPages(Long userId, YearMonth date) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;
        QMeetMember meetMember = QMeetMember.meetMember;
        QPlanParticipant participant = QPlanParticipant.planParticipant;

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
                                plan.participants.size(),
                                plan.weatherIcon,
                                plan.weatherAddress,
                                plan.temperature,
                                plan.pop
                        )
                )
                .from(meet)
                .rightJoin(meet.plans, plan)
                .on(
                        plan.participants.contains(
                                JPAExpressions
                                        .selectFrom(participant)
                                        .where(participant.user.id.eq(userId).and(participant.plan.id.eq(plan.id)))
                        ),
                        getBetweenDate(plan.planTime, start, end)
                )
                .where(
                        meet.members.contains(
                                JPAExpressions
                                        .selectFrom(meetMember)
                                        .where(meet.id.eq(meetMember.joinMeet.id).and(meetMember.user.id.eq(userId)))
                        )
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
                                review.participants.size(),
                                review.weatherIcon,
                                review.weatherAddress,
                                review.temperature,
                                review.pop
                        )
                )
                .from(meet)
                .rightJoin(meet.reviews, review)
                .on(
                        review.participants.contains(
                                JPAExpressions
                                        .selectFrom(participant)
                                        .where(participant.user.id.eq(userId).and(participant.review.id.eq(review.id)))
                        ),
                        getBetweenDate(review.planTime, start, end)
                )
                .where(
                        meet.members.contains(
                                JPAExpressions
                                        .selectFrom(meetMember)
                                        .where(meet.id.eq(meetMember.joinMeet.id).and(meetMember.user.id.eq(userId)))
                        )
                )
                .fetch();

        return new UserPageResponse(reviews, plans);
    }

    public UserAllDateResponse getAllDate(Long userId) {
        QMeet meet = QMeet.meet;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;
        QMeetMember meetMember = QMeetMember.meetMember;
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        List<LocalDateTime> planDate = queryFactory
                .select(plan.planTime)
                .from(meet)
                .rightJoin(meet.plans, plan)
                .on(
                        plan.participants.contains(
                                JPAExpressions
                                        .selectFrom(participant)
                                        .where(participant.user.id.eq(userId).and(participant.plan.id.eq(plan.id)))
                        )
                )
                .where(
                        meet.members.contains(
                                JPAExpressions
                                        .selectFrom(meetMember)
                                        .where(meet.id.eq(meetMember.joinMeet.id).and(meetMember.user.id.eq(userId)))
                        )
                )
                .fetch();

        List<LocalDateTime> reviewDate = queryFactory
                .select(review.planTime)
                .from(meet)
                .rightJoin(meet.reviews, review)
                .on(
                        review.participants.contains(
                                JPAExpressions
                                        .selectFrom(participant)
                                        .where(participant.user.id.eq(userId).and(participant.review.id.eq(review.id)))
                        )
                )
                .where(
                        meet.members.contains(
                                JPAExpressions
                                        .selectFrom(meetMember)
                                        .where(meet.id.eq(meetMember.joinMeet.id).and(meetMember.user.id.eq(userId)))
                        )
                )
                .fetch();

        return new UserAllDateResponse(planDate, reviewDate);
    }

    private BooleanExpression getBetweenDate(DateTimePath<LocalDateTime> planTime, LocalDateTime start, LocalDateTime end) {
        return planTime.between(start, end);
    }

    public List<MeetPlan> findUpdateWeatherPlan() {
        QMeetPlan plan = QMeetPlan.meetPlan;
        var now = LocalDateTime.now();
        return queryFactory
                .selectFrom(plan)
                .where(plan.planTime.before(now.plusDays(5)))
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
