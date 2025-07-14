package com.groupMeeting.notification.utils;

import com.groupMeeting.dto.response.notification.NotifySendRequest;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.PushTopic;
import com.groupMeeting.notification.reader.NotificationUserReader;
import com.groupMeeting.notification.reader.PushTokenReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotifySendRequestFactory {
    private final NotificationUserReader userReader;
    private final PushTokenReader tokenReader;

    public NotifySendRequest getMeetPushToken(Long userId, Long meetId, PushTopic pushTopic) {

        List<User> allUser = userReader.findMeetAllUser(meetId, userId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getPlanPushToken(Long userId, Long planId, PushTopic pushTopic) {

        List<User> allUser = userReader.findPlanUsers(planId, userId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getPlanRemindToken(Long planId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllPlanUser(planId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getReviewCreatorPushToken(Long creatorId, Long reviewId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllReviewCreatorUser(creatorId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findAllUserId(allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getReviewPushToken(Long userId, Long reviewId, PushTopic pushTopic) {

        List<User> allUser = userReader.findAllReviewUser(userId, reviewId);
        List<Long> users = tokenReader.findAllTokenId(userReader.findUserIds(userId, allUser), pushTopic);

        return new NotifySendRequest(
                allUser,
                tokenReader.findPushToken(users)
        );
    }

    public NotifySendRequest getCommentReplyPushToken(Long userId, Long parentCommentId, PushTopic pushTopic) {

        List<User> user = userReader.findParentCommentUser(userId, parentCommentId);
        List<Long> userToken = tokenReader.findAllTokenId(userReader.findAllUserId(user), pushTopic);

        return new NotifySendRequest(
                user,
                tokenReader.findPushToken(userToken)
        );
    }

    public NotifySendRequest getCommentMentionPushToken(List<Long> originMentions, Long userId, Long commentId, PushTopic pushTopic) {

        List<User> mentionedUsers = userReader.filterNewMentionedUsers(originMentions, userId, commentId);
        List<Long> usersTokens = tokenReader.findAllTokenId(userReader.findAllUserId(mentionedUsers), pushTopic);

        return new NotifySendRequest(
                mentionedUsers,
                tokenReader.findPushToken(usersTokens)
        );
    }

}
