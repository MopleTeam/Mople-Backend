package com.mople.notification.utils;

import com.mople.dto.response.notification.NotifySendRequest;
import com.mople.entity.user.User;
import com.mople.global.enums.PushTopic;
import com.mople.notification.reader.NotificationUserReader;
import com.mople.notification.reader.PushTokenReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotifySendRequestFactory {
    private final NotificationUserReader userReader;
    private final PushTokenReader tokenReader;

    public NotifySendRequest getMeetPushTokens(Long triggeredBy, Long meetId, PushTopic pushTopic) {

        List<User> allUser = userReader.findMeetAllUser(triggeredBy, meetId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(triggeredBy, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getPlanPushTokens(Long triggeredBy, Long planId, PushTopic pushTopic) {

        List<User> allUser = userReader.findPlanUsers(triggeredBy, planId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(triggeredBy, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getPlanPushTokensAll(Long planId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllPlanUser(planId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getCreatorPushToken(Long creatorId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllReviewCreatorUser(creatorId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getReviewPushToken(Long triggeredBy, Long reviewId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllReviewUser(triggeredBy, reviewId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(triggeredBy, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getCommentReplyPushToken(Long senderId, Long parentCommentId, PushTopic pushTopic) {

        List<User> user = userReader.findParentCommentUser(senderId, parentCommentId);
        List<Long> userToken = tokenReader.findAllTokenId(userReader.findAllUserId(user), pushTopic);

        return new NotifySendRequest(
                user,
                tokenReader.findPushToken(userToken)
        );
    }

    public NotifySendRequest getCommentMentionPushToken(List<Long> originMentions, Long senderId, Long commentId, PushTopic pushTopic) {

        List<User> mentionedUsers = userReader.filterNewMentionedUsers(originMentions, senderId, commentId);
        List<Long> usersTokens = tokenReader.findAllTokenId(userReader.findAllUserId(mentionedUsers), pushTopic);

        return new NotifySendRequest(
                mentionedUsers,
                tokenReader.findPushToken(usersTokens)
        );
    }

}
