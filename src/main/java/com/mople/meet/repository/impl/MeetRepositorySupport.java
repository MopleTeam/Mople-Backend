package com.mople.meet.repository.impl;

import com.mople.dto.response.meet.MeetListFindMemberResponse;
import com.mople.dto.response.meet.MeetListResponse;

import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.QMeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.QPlanReview;
import com.mople.global.enums.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.mople.entity.meet.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.*;

import static java.util.stream.Collectors.*;

@Repository
@RequiredArgsConstructor
public class MeetRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<Meet> findMeetPage(Long userId, Long cursorId, int size) {
        QMeet meet = QMeet.meet;
        QMeetMember meetMember = QMeetMember.meetMember;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(meet.status.eq(Status.ACTIVE))
                .and(meetMember.userId.eq(userId));

        if (cursorId != null) {
            whereCondition.and(meet.id.gt(cursorId));
        }

        return queryFactory
                .selectFrom(meet)
                .join(meetMember).on(meet.id.eq(meetMember.meetId))
                .where(whereCondition)
                .orderBy(meet.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<MeetListResponse> mapToMeetListResponses(List<Meet> meets) {
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;

        List<Long> meetIdList = meets.stream().map(Meet::getId).toList();

        List<MeetPlan> plans = queryFactory.selectFrom(plan)
                .where(
                        plan.status.eq(Status.ACTIVE),
                        plan.meetId.in(meetIdList)
                )
                .orderBy(plan.planTime.desc())
                .fetch();

        Map<Long, Optional<MeetPlan>> planMap =
                plans.stream()
                        .collect(
                                groupingBy(
                                        MeetPlan::getMeetId,
                                        minBy(Comparator.comparing(MeetPlan::getPlanTime))
                                )
                        );

        List<PlanReview> reviews = queryFactory.selectFrom(review)
                .where(
                        review.status.eq(Status.ACTIVE),
                        review.meetId.in(meetIdList)
                )
                .orderBy(review.planTime.asc())
                .fetch();

        Map<Long, Optional<PlanReview>> reviewMap =
                reviews
                        .stream()
                        .collect(
                                groupingBy(
                                        PlanReview::getMeetId,
                                        maxBy(Comparator.comparing(PlanReview::getPlanTime))
                                )
                        );

        return meets.stream()
                .map(m -> {
                    if (planMap.containsKey(m.getId()) && planMap.get(m.getId()).isPresent()) {
                        MeetPlan meetPlan = planMap.get(m.getId()).get();

                        return new MeetListResponse(
                                m.getId(),
                                m.getVersion(),
                                m.getName(),
                                m.getMeetImage(),
                                countMeetMember(m.getId()),
                                meetPlan.getPlanTime()
                        );
                    }

                    if (reviewMap.containsKey(m.getId()) && reviewMap.get(m.getId()).isPresent()) {
                        PlanReview planReview = reviewMap.get(m.getId()).get();

                        return new MeetListResponse(
                                m.getId(),
                                m.getVersion(),
                                m.getName(),
                                m.getMeetImage(),
                                countMeetMember(m.getId()),
                                planReview.getPlanTime()
                        );
                    }

                    return new MeetListResponse(
                            m.getId(),
                            m.getVersion(),
                            m.getName(),
                            m.getMeetImage(),
                            countMeetMember(m.getId()),
                            null
                    );
                })
                .toList();
    }

    public List<MeetListFindMemberResponse> findMeetUseMember(Long userId) {
        QMeet meet = QMeet.meet;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory.select(
                        Projections.constructor(
                                MeetListFindMemberResponse.class,
                                meet.id,
                                meet.name,
                                meet.meetImage
                        )
                )
                .from(meet)
                .join(meetMember).on(meetMember.meetId.eq(meet.id))
                .where(
                        meet.status.eq(Status.ACTIVE),
                        meetMember.userId.eq(userId)
                )
                .distinct()
                .fetch();
    }

    public Integer countMeetMember(Long meetId) {
        QMeetMember member = QMeetMember.meetMember;

        Long count = queryFactory
                .select(member.count())
                .from(member)
                .where(member.meetId.eq(meetId))
                .fetchOne();

        return Math.toIntExact(count != null ? count : 0);
    }

    public boolean isCursorInvalid(Long cursorId) {
        QMeet meet = QMeet.meet;

        return queryFactory
                .selectOne()
                .from(meet)
                .where(
                        meet.status.eq(Status.ACTIVE),
                        meet.id.eq(cursorId)
                )
                .fetchFirst() == null;
    }
}
