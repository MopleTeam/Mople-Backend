package com.mople.meet.repository.impl.review;

import com.mople.entity.meet.review.QReviewImage;
import com.mople.entity.meet.review.ReviewImage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReviewImageRepositorySupport {

    private final JPAQueryFactory queryFactory;

    public Map<Long, List<ReviewImage>> reviewImageMap(List<Long> reviewIds) {
        QReviewImage image = QReviewImage.reviewImage1;

         return queryFactory
                .selectFrom(image)
                .where(image.reviewId.in(reviewIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(ReviewImage::getReviewId));
    }
}
