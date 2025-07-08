package com.groupMeeting.notification.reader;

import com.groupMeeting.entity.meet.QMeetMember;
import com.groupMeeting.entity.meet.comment.QCommentMention;
import com.groupMeeting.entity.meet.comment.QPlanComment;
import com.groupMeeting.entity.meet.plan.QPlanParticipant;
import com.groupMeeting.entity.user.QUser;
import com.groupMeeting.entity.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationUserReader {
    private final JPAQueryFactory queryFactory;

    public List<User> findMeetAllUser(Long meetId, Long userId) {

        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(meetMember.user)
                .from(meetMember)
                .where(meetMember.joinMeet.id.eq(meetId), meetMember.user.id.ne(userId))
                .fetch();
    }

    public List<User> findPlanUsers(Long planId, Long userId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.plan.id.eq(planId), participant.user.id.ne(userId))
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

    public List<User> findAllReviewUser(Long userId, Long reviewId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.review.id.eq(reviewId), participant.user.id.ne(userId))
                .fetch();
    }

    public List<User> findAllReviewCreatorUser(Long creatorId) {

        QUser user = QUser.user;

        return queryFactory
                .selectFrom(user)
                .where(user.id.eq(creatorId))
                .fetch();
    }

    public List<User> findParentCommentUser(Long senderId, Long commentId) {

        QPlanComment planComment = QPlanComment.planComment;

        return queryFactory
                .select(planComment.writer)
                .from(planComment)
                .where(planComment.id.eq(commentId), planComment.writer.id.ne(senderId))
                .fetch();
    }

    public List<User> findMentionedUsers(Long senderId, Long commentId) {

        QCommentMention mention = QCommentMention.commentMention;
        QUser user = QUser.user;

        return queryFactory
                .select(user)
                .distinct()
                .from(mention)
                .join(mention.mentionedUser, user)
                .where(mention.comment.id.eq(commentId), user.id.ne(senderId))
                .fetch();
    }

    public List<Long> findAllUserId(List<User> users) {

        return users.stream().map(User::getId).toList();
    }

    public List<Long> findUserIds(Long userId, List<User> users) {

        return users.stream()
                .map(User::getId)
                .filter(id -> !id.equals(userId))
                .toList();
    }
}
