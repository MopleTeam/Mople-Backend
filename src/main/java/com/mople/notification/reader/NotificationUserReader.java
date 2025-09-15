package com.mople.notification.reader;

import com.mople.entity.meet.QMeetMember;
import com.mople.entity.meet.comment.QCommentMention;
import com.mople.entity.meet.comment.QPlanComment;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.user.QUser;
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

    public List<Long> findReviewUsersNoTriggers(Long triggeredBy, Long reviewId) {
        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.userId)
                .from(participant)
                .where(participant.reviewId.eq(reviewId), participant.userId.ne(triggeredBy))
                .fetch();
    }

    public List<Long> findReviewCreator(Long creatorId) {
        QUser user = QUser.user;

        return queryFactory
                .select(user.id)
                .from(user)
                .where(user.id.eq(creatorId))
                .fetch();
    }

    public Long findCommentRepliedUserNoWriter(Long senderId, Long parentCommentId) {
        QPlanComment planComment = QPlanComment.planComment;

        return queryFactory
                .select(planComment.writerId)
                .from(planComment)
                .where(planComment.id.eq(parentCommentId), planComment.writerId.ne(senderId))
                .fetchOne();
    }

    private List<Long> findCommentMentionedUsersNoWriter(Long senderId, Long commentId) {
        QCommentMention mention = QCommentMention.commentMention;

        return queryFactory
                .select(mention.userId)
                .from(mention)
                .where(
                        mention.commentId.eq(commentId),
                        mention.userId.ne(senderId)
                )
                .fetch();
    }

    public List<Long> findCreatedMentionedUsers(Long senderId, Long commentId) {
        return findCommentMentionedUsersNoWriter(senderId, commentId);
    }

    public List<Long> findUpdatedMentionedUsers(List<Long> originMentions, Long senderId, Long commentId) {
        List<Long> targetIds = findCommentMentionedUsersNoWriter(senderId, commentId);

        return targetIds.stream()
                .filter(targetId -> !originMentions.contains(targetId))
                .toList();
    }
}
