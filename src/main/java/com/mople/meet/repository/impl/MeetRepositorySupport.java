package com.mople.meet.repository.impl;

import com.mople.dto.response.meet.MeetListFindMemberResponse;
import com.mople.dto.response.meet.MeetListResponse;

import com.mople.entity.meet.plan.MeetPlan;
import com.mople.entity.meet.plan.QMeetPlan;
import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.QPlanReview;
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

    public List<Meet> findMeetFirstPage(Long userId, int size) {
        QMeet meet = QMeet.meet;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .selectFrom(meet)
                .join(meetMember).on(meet.id.eq(meetMember.joinMeet.id))
                .where(meetMember.user.id.eq(userId))
                .orderBy(meet.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<Meet> findMeetNextPage(Long userId, Long cursorId, int size) {
        QMeet meet = QMeet.meet;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .selectFrom(meet)
                .join(meetMember).on(meet.id.eq(meetMember.joinMeet.id))
                .where(
                        meetMember.user.id.eq(userId)
                                .and(meet.id.gt(cursorId))
                )
                .orderBy(meet.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<MeetListResponse> mapToMeetListResponses(List<Meet> meets) {
        QMeetPlan plan = QMeetPlan.meetPlan;
        QPlanReview review = QPlanReview.planReview;

        List<Long> meetIdList = meets.stream().map(Meet::getId).toList();

        List<MeetPlan> plans = queryFactory.selectFrom(plan)
                .where(plan.meet.id.in(meetIdList))
                .orderBy(plan.planTime.desc())
                .fetch();

        Map<Long, Optional<MeetPlan>> planMap =
                plans.stream()
                        .collect(
                                groupingBy(
                                        p -> p.getMeet().getId(),
                                        minBy(Comparator.comparing(MeetPlan::getPlanTime))
                                )
                        );

        List<PlanReview> reviews = queryFactory.selectFrom(review)
                .where(review.meet.id.in(meetIdList))
                .orderBy(review.planTime.asc())
                .fetch();

        Map<Long, Optional<PlanReview>> reviewMap =
                reviews
                        .stream()
                        .collect(
                                groupingBy(
                                        r -> r.getMeet().getId(),
                                        maxBy(Comparator.comparing(PlanReview::getPlanTime))
                                )
                        );

        return meets.stream()
                .map(m -> {
                    if (planMap.containsKey(m.getId()) && planMap.get(m.getId()).isPresent()) {
                        MeetPlan meetPlan = planMap.get(m.getId()).get();

                        return new MeetListResponse(
                                m.getId(),
                                m.getName(),
                                m.getMeetImage(),
                                m.getMembers().size(),
                                meetPlan.getPlanTime()
                        );
                    }

                    if (reviewMap.containsKey(m.getId()) && reviewMap.get(m.getId()).isPresent()) {
                        PlanReview planReview = reviewMap.get(m.getId()).get();

                        return new MeetListResponse(
                                m.getId(),
                                m.getName(),
                                m.getMeetImage(),
                                m.getMembers().size(),
                                planReview.getPlanTime()
                        );
                    }

                    return new MeetListResponse(
                            m.getId(),
                            m.getName(),
                            m.getMeetImage(),
                            m.getMembers().size(),
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
                .join(meet.members, meetMember)
                .on(meet.id.eq(meetMember.joinMeet.id))
                .where(meetMember.user.id.eq(userId))
                .fetch();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QMeet meet = QMeet.meet;

        return queryFactory
                .selectOne()
                .from(meet)
                .where(meet.id.eq(cursorId))
                .fetchFirst() == null;
    }
}
