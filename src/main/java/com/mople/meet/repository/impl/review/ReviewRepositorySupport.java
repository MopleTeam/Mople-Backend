package com.mople.meet.repository.impl.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.QPlanReview;
import com.mople.global.enums.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanReview> findReviewPage(Long meetId, Long cursorId, int size) {
        QPlanReview review = QPlanReview.planReview;

        BooleanBuilder whereCondition = new BooleanBuilder()
                .and(review.status.eq(Status.ACTIVE))
                .and(review.meetId.eq(meetId));

        if (cursorId != null) {
            LocalDateTime cursorPlanTime = queryFactory
                    .select(review.planTime)
                    .from(review)
                    .where(review.id.eq(cursorId))
                    .fetchOne();

            whereCondition.and(
                    review.planTime.lt(cursorPlanTime)
                            .or(review.planTime.eq(cursorPlanTime).and(review.id.lt(cursorId)))
            );
        }

        return queryFactory
                .selectFrom(review)
                .where(whereCondition)
                .orderBy(review.planTime.desc(), review.id.desc())
                .limit(size + 1)
                .fetch();
    }

    public boolean isCursorInvalid(Long cursorId) {
        QPlanReview review = QPlanReview.planReview;

        return queryFactory
                .selectOne()
                .from(review)
                .where(
                        review.status.eq(Status.ACTIVE),
                        review.id.eq(cursorId)
                )
                .fetchFirst() == null;
    }
}
