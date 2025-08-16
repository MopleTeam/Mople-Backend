package com.mople.notification.reader;

import com.mople.entity.meet.QMeetMember;
import com.mople.entity.meet.comment.QCommentMention;
import com.mople.entity.meet.comment.QPlanComment;
import com.mople.entity.meet.plan.QPlanParticipant;
import com.mople.entity.user.QUser;
import com.mople.entity.user.User;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationUserReader {
    private final JPAQueryFactory queryFactory;

    public List<User> findMeetAllUser(Long triggeredBy, Long meetId) {

        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(meetMember.user)
                .from(meetMember)
                .where(meetMember.joinMeet.id.eq(meetId), meetMember.user.id.ne(triggeredBy))
                .fetch();
    }

    public List<User> findPlanUsers(Long triggeredBy, Long planId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.plan.id.eq(planId), participant.user.id.ne(triggeredBy))
                .fetch();
    }

    public List<User> findAllPlanUser(Long planId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.plan.id.eq(planId))
                .fetch();
    }

    public List<User> findAllReviewUser(Long creatorId, Long reviewId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.review.id.eq(reviewId), participant.user.id.ne(creatorId))
                .fetch();
    }

    public List<User> findAllReviewCreatorUser(Long creatorId) {

        QUser user = QUser.user;

        return queryFactory
                .selectFrom(user)
                .where(user.id.eq(creatorId))
                .fetch();
    }

    public List<User> findParentCommentUser(Long senderId, Long parentCommentId) {

        QPlanComment planComment = QPlanComment.planComment;

        return queryFactory
                .select(planComment.writer)
                .from(planComment)
                .where(planComment.id.eq(parentCommentId), planComment.writer.id.ne(senderId))
                .fetch();
    }

    public List<User> findMentionedUsers(Long senderId, Long commentId) {

        QCommentMention mention = QCommentMention.commentMention;
        QUser user = QUser.user;

        return queryFactory
                .select(user)
                .distinct()
                .from(user)
                .where(
                        user.id.in(
                                JPAExpressions
                                        .select(mention.userId)
                                        .from(mention)
                                        .where(mention.commentId.eq(commentId))
                        ),
                        user.id.ne(senderId)
                )
                .fetch();
    }

    public List<User> filterNewMentionedUsers(List<Long> originMentions, Long senderId, Long commentId) {
        List<User> mentionedUsers = findMentionedUsers(senderId, commentId);

        if (originMentions == null || originMentions.isEmpty()) return mentionedUsers;

        return mentionedUsers.stream()
                .filter(mentionUser -> !originMentions.contains(mentionUser.getId()))
                .toList();
    }

    public List<Long> findAllUserId(List<User> users) {

        return users.stream().map(User::getId).toList();
    }

    public List<Long> findUserIds(Long triggeredBy, List<User> users) {

        return users.stream()
                .map(User::getId)
                .filter(id -> !id.equals(triggeredBy))
                .toList();
    }
}
