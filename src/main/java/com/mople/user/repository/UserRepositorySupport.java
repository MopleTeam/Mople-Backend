package com.mople.user.repository;

import com.mople.entity.meet.QMeet;
import com.mople.entity.meet.QMeetMember;
import com.mople.entity.meet.comment.QPlanComment;
import com.mople.entity.meet.plan.QMeetPlan;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.meet.review.QPlanReview;
import com.mople.entity.meet.review.QReviewImage;
import com.mople.entity.notification.QFirebaseToken;
import com.mople.entity.notification.QNotification;
import com.mople.entity.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public void removeUser(Long userId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QPlanComment comment = QPlanComment.planComment;
        QNotification notification = QNotification.notification;
        QFirebaseToken token = QFirebaseToken.firebaseToken;
        QPlanReview review = QPlanReview.planReview;
        QReviewImage reviewImage = QReviewImage.reviewImage1;
        QMeet meet = QMeet.meet;
        QMeetMember meetMember = QMeetMember.meetMember;
        QMeetPlan plan = QMeetPlan.meetPlan;
        QUser user = QUser.user;

        // participants
        List<Long> meets = queryFactory.select(meet.id).from(meet).where(meet.creator.id.eq(userId)).fetch();
        List<Long> reviewMeetIds = queryFactory.select(review.id).from(review).where(review.meet.id.in(meets)).fetch();
        List<Long> reviewIds = queryFactory.select(review.id).from(review).where(review.creatorId.eq(userId)).fetch();
        List<Long> planIds = queryFactory.select(plan.id).from(plan).where(plan.creator.id.eq(userId)).fetch();
        List<Long> planMeetIds = queryFactory.select(plan.id).from(plan).where(plan.meet.id.in(meets)).fetch();

        queryFactory.delete(participant).where(participant.user.id.eq(userId)).execute();
        queryFactory.delete(participant).where(participant.review.id.in(reviewIds)).execute();
        queryFactory.delete(participant).where(participant.review.id.in(reviewMeetIds)).execute();
        queryFactory.delete(participant).where(participant.plan.id.in(planIds)).execute();
        queryFactory.delete(participant).where(participant.plan.id.in(planMeetIds)).execute();

        // plan, review
        queryFactory.delete(reviewImage).where(reviewImage.review.id.in(reviewIds)).execute();
        queryFactory.delete(reviewImage).where(reviewImage.review.id.in(reviewMeetIds)).execute();

        queryFactory.delete(review).where(review.id.in(reviewIds)).execute();
        queryFactory.delete(review).where(review.meet.id.in(meets)).execute();

        queryFactory.delete(plan).where(plan.id.in(planIds)).execute();
        queryFactory.delete(plan).where(plan.meet.id.in(meets)).execute();

        // meet, meetMember
        queryFactory.delete(meetMember).where(meetMember.user.id.eq(userId)).execute();
        queryFactory.delete(meetMember).where(meetMember.joinMeet.id.in(meets)).execute();
        queryFactory.delete(meet).where(meet.id.in(meets)).execute();

        // etc
        queryFactory.delete(comment).where(comment.writer.id.eq(userId)).execute();
        queryFactory.delete(notification).where(notification.user.id.eq(userId)).execute();
        queryFactory.delete(token).where(token.userId.eq(userId)).execute();

        // user
        queryFactory.delete(user).where(user.id.eq(userId)).execute();
    }
}
