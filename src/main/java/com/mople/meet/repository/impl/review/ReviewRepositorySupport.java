package com.mople.meet.repository.impl.review;

import com.mople.entity.meet.review.PlanReview;
import com.mople.entity.meet.review.QPlanReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public List<PlanReview> findReviewFirstPage(Long meetId, int size) {
        QPlanReview review = QPlanReview.planReview;

        return queryFactory
                .selectFrom(review)
                .where(review.meet.id.eq(meetId))
                .orderBy(review.planTime.desc(), review.id.asc())
                .limit(size + 1)
                .fetch();
    }

    public List<PlanReview> findReviewNextPage(Long meetId, Long cursorId, int size) {
        QPlanReview review = QPlanReview.planReview;

        LocalDateTime cursorPlanTime = queryFactory
                .select(review.planTime)
                .from(review)
                .where(review.id.eq(cursorId))
                .fetchOne();

        return queryFactory
                .selectFrom(review)
                .where(
                        review.meet.id.eq(meetId),
                        review.planTime.lt(cursorPlanTime)
                                .or(review.planTime.eq(cursorPlanTime).and(review.id.lt(cursorId)))
                )
                .orderBy(review.planTime.desc(), review.id.desc())
                .limit(size + 1)
                .fetch();
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
