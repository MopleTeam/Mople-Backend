package com.mople.meet.repository.impl.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.QPlanReview;
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
                .and(review.meet.id.eq(meetId));

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

    public Long countReviews(Long meetId) {
        QPlanReview review = QPlanReview.planReview;

        Long count = queryFactory
                .select(review.count())
                .from(review)
                .where(review.meet.id.eq(meetId))
                .fetchOne();

        return count != null ? count : 0L;
    }

    public boolean isCursorInvalid(Long cursorId) {
        QPlanReview review = QPlanReview.planReview;

        return queryFactory
                .selectOne()
                .from(review)
                .where(review.id.eq(cursorId))
                .fetchFirst() == null;
    }

}
