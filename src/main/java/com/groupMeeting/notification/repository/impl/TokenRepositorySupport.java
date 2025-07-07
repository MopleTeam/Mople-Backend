package com.groupMeeting.notification.repository.impl;

import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.meet.QMeetMember;
import com.groupMeeting.entity.meet.comment.QCommentMention;
import com.groupMeeting.entity.meet.comment.QPlanComment;
import com.groupMeeting.entity.meet.plan.QPlanParticipant;
import com.groupMeeting.entity.notification.FirebaseToken;
import com.groupMeeting.entity.notification.QFirebaseToken;
import com.groupMeeting.entity.notification.QTopic;
import com.groupMeeting.entity.user.QUser;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.PushTopic;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TokenRepositorySupport {
    private final JPAQueryFactory queryFactory;

    public NotifySendRequest getMeetPushToken(Long userId, Long meetId, PushTopic pushTopic) {

        List<User> allUser = getMeetAllUser(meetId, userId);
        List<Long> users = getAllTokenId(getUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                getPushToken(users)
        );
    }

    public NotifySendRequest getPlanPushToken(Long userId, Long planId, PushTopic pushTopic) {

        List<User> allUser = getPlanUsers(planId, userId);
        List<Long> users = getAllTokenId(getUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                getPushToken(users)
        );
    }

    public NotifySendRequest getPlanRemindToken(Long planId, PushTopic pushTopic) {

        List<User> allUser = getAllPlanUser(planId);
        List<Long> users = getAllTokenId(getAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                getPushToken(users)
        );
    }

    public NotifySendRequest getReviewCreatorPushToken(Long creatorId, Long reviewId, PushTopic pushTopic) {

        QUser user = QUser.user;

        List<User> allUser = queryFactory
                .selectFrom(user)
                .where(user.id.eq(creatorId))
                .fetch();

        List<Long> users = getAllTokenId(getAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                getPushToken(users)
        );
    }

    public NotifySendRequest getReviewPushToken(Long userId, Long reviewId, PushTopic pushTopic) {

        List<User> allUser = getAllReviewUser(userId, reviewId);
        List<Long> users = getAllTokenId(getUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                getPushToken(users)
        );
    }

    public NotifySendRequest getCommentReplyPushToken(Long userId, Long commentId, PushTopic pushTopic) {

        List<User> user = getParentCommentUser(userId, commentId);
        List<Long> userToken = getAllTokenId(getAllUserId(user), pushTopic);

        return new NotifySendRequest(
                user,
                getPushToken(userToken)
        );
    }

    public NotifySendRequest getCommentMentionPushToken(Long userId, Long commentId, PushTopic pushTopic) {

        List<User> mentionedUsers = getMentionedUsers(userId, commentId);
        List<Long> usersTokens = getAllTokenId(getAllUserId(mentionedUsers), pushTopic);

        return new NotifySendRequest(
                mentionedUsers,
                getPushToken(usersTokens)
        );
    }

    private List<FirebaseToken> getPushToken(List<Long> users) {

        QFirebaseToken token = QFirebaseToken.firebaseToken;

        return queryFactory
                .select(token)
                .from(token)
                .where(token.userId.in(users), token.active.isTrue())
                .fetch();
    }

    private List<User> getMeetAllUser(Long meetId, Long userId) {

        QMeetMember meetMember = QMeetMember.meetMember;

        return queryFactory
                .select(meetMember.user)
                .from(meetMember)
                .where(meetMember.joinMeet.id.eq(meetId), meetMember.user.id.ne(userId))
                .fetch();
    }

    private List<User> getPlanUsers(Long planId, Long userId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.plan.id.eq(planId), participant.user.id.ne(userId))
                .fetch();
    }

    private List<User> getAllPlanUser(Long planId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.plan.id.eq(planId))
                .fetch();
    }

    private List<User> getAllReviewUser(Long userId, Long reviewId) {

        QPlanParticipant participant = QPlanParticipant.planParticipant;

        return queryFactory
                .select(participant.user)
                .from(participant)
                .where(participant.review.id.eq(reviewId), participant.user.id.ne(userId))
                .fetch();
    }

    private List<User> getParentCommentUser(Long userId, Long commentId) {

        QPlanComment planComment = QPlanComment.planComment;
        QUser user = QUser.user;

        return queryFactory
                .select(user)
                .from(planComment)
                .join(user).on(user.id.eq(planComment.writer.id))
                .where(planComment.id.eq(commentId), user.id.ne(userId))
                .fetch();
    }

    private List<User> getMentionedUsers(Long userId, Long commentId) {

        QPlanComment planComment = QPlanComment.planComment;
        QCommentMention mention = QCommentMention.commentMention;
        QUser user = QUser.user;

        return queryFactory
                .select(user)
                .from(planComment)
                .join(planComment.mentions, mention)
                .join(mention.mentionedUser, user)
                .where(planComment.id.eq(commentId), user.id.ne(userId))
                .fetch();
    }

    private List<Long> getAllTokenId(List<Long> users, PushTopic pushTopic) {

        QTopic topic = QTopic.topic1;

        return queryFactory
                .select(topic.userId)
                .from(topic)
                .where(topic.userId.in(users), topic.topic.eq(pushTopic))
                .fetch();
    }

    private List<Long> getAllUserId(List<User> users) {

        return users.stream().map(User::getId).toList();
    }

    private List<Long> getUserIds(Long userId, List<User> users) {

        return users.stream()
                .map(User::getId)
                .filter(id -> !id.equals(userId))
                .toList();
    }
}
