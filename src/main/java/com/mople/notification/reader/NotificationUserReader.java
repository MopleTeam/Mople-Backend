package com.mople.notification.reader;

import com.mople.entity.meet.QMeetMember;
import com.mople.entity.meet.comment.QCommentMention;
import com.mople.entity.meet.comment.QMeetComment;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.user.QUser;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationUserReader {
    private final JPAQueryFactory queryFactory;

    public List<Long> findMeetUsersNoTriggers(Long triggeredBy, Long meetId) {
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(meetMember.userId)
                .from(meetMember)
                .where(meetMember.meetId.eq(meetId), meetMember.userId.ne(triggeredBy))
                .fetch();
    }

    public List<Long> findPlanUsersNoTriggers(Long triggeredBy, Long planId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.userId)
                .from(participant)
                .where(participant.planId.eq(planId), participant.userId.ne(triggeredBy))
                .fetch();
    }

    public List<Long> findPlanUsersAll(Long planId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.userId)
                .from(participant)
                .where(participant.planId.eq(planId))
                .fetch();
    }

    public List<Long> findReviewUsersNoTriggers(Long triggeredBy, Long reviewId, Long meetId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(participant.userId)
                .from(participant)
                .where(
                        participant.reviewId.eq(reviewId),
                        participant.userId.ne(triggeredBy),
                        JPAExpressions
                                .selectOne()
                                .from(meetMember)
                                .where(
                                        meetMember.meetId.eq(meetId),
                                        meetMember.userId.eq(participant.userId)
                                )
                                .exists()
                )
                .fetch();
    }

    public List<Long> findPlanReviewCreator(Long creatorId) {
        QUser user = QUser.user;

        return queryFactory
                .select(user.id)
                .from(user)
                .where(user.id.eq(creatorId))
                .fetch();
    }

    public Long findCommentRepliedUserNoWriter(Long senderId, Long parentCommentId, Long meetId) {
        QMeetComment meetComment = QMeetComment.meetComment;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(meetComment.writerId)
                .from(meetComment)
                .where(
                        meetComment.id.eq(parentCommentId),
                        meetComment.writerId.ne(senderId),
                        JPAExpressions
                                .selectOne()
                                .from(meetMember)
                                .where(
                                        meetMember.meetId.eq(meetId),
                                        meetMember.userId.eq(meetComment.writerId)
                                )
                                .exists()
                )
                .fetchOne();
    }

    private List<Long> findCommentMentionedUsersNoWriter(Long senderId, Long commentId, Long meetId) {
        QCommentMention mention = QCommentMention.commentMention;
        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .selectDistinct(mention.userId)
                .from(mention)
                .where(
                        mention.commentId.eq(commentId),
                        mention.userId.ne(senderId),
                        JPAExpressions
                                .selectOne()
                                .from(meetMember)
                                .where(
                                        meetMember.meetId.eq(meetId),
                                        meetMember.userId.eq(mention.userId)
                                )
                                .exists()
                )
                .fetch();
    }

    public List<Long> findCreatedMentionedUsers(Long senderId, Long commentId, Long meetId) {
        return findCommentMentionedUsersNoWriter(senderId, commentId, meetId);
    }

    public List<Long> findUpdatedMentionedUsers(List<Long> originMentions, Long senderId, Long commentId, Long meetId) {
        List<Long> targetIds = findCommentMentionedUsersNoWriter(senderId, commentId, meetId);

        return targetIds.stream()
                .filter(targetId -> !originMentions.contains(targetId))
                .toList();
    }
}
