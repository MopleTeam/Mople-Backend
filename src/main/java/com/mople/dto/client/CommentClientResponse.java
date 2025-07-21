package com.mople.dto.client;

import com.mople.dto.response.meet.comment.CommentResponse;
import com.mople.dto.response.meet.comment.CommentUpdateResponse;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentClientResponse {
    private final Long commentId;
    private final String content;
    private final Long postId;
    private final Long parentId;
    private final Integer replyCount;
    private final Integer likeCount;
    private final boolean likedByMe;
    private final UserInfo writer;
    private final List<UserInfo> mentions;
    private final LocalDateTime time;

    public static List<CommentClientResponse> ofComments(List<CommentResponse> commentResponses) {
        return commentResponses.stream().map(CommentClientResponse::ofComment).toList();
    }

    public static CommentClientResponse ofComment(CommentResponse commentResponse) {
        return CommentClientResponse.builder()
                .commentId(commentResponse.commentId())
                .content(commentResponse.content())
                .postId(commentResponse.postId())
                .parentId(commentResponse.parentId())
                .replyCount(commentResponse.replyCount())
                .likeCount(commentResponse.likeCount())
                .likedByMe(commentResponse.likedByMe())
                .writer(ofWriter(commentResponse.writer()))
                .mentions(ofMentions(commentResponse.mentions()))
                .time(commentResponse.time())
                .build();
    }

    public static CommentClientResponse ofUpdate(CommentUpdateResponse updateResponse) {
        return CommentClientResponse.builder()
                .commentId(updateResponse.commentId())
                .content(updateResponse.content())
                .postId(updateResponse.postId())
                .parentId(updateResponse.parentId())
                .replyCount(updateResponse.replyCount())
                .likeCount(updateResponse.likeCount())
                .likedByMe(updateResponse.likedByMe())
                .writer(ofWriter(updateResponse.writer()))
                .mentions(ofMentions(updateResponse.mentions()))
                .time(updateResponse.time())
                .build();
    }

    private static UserInfo ofWriter(User writer) {
        return buildUserInfo(writer);
    }

    private static List<UserInfo> ofMentions(List<User> mentions) {
        return mentions.stream()
                .map(CommentClientResponse::buildUserInfo)
                .toList();
    }

    private static UserInfo buildUserInfo(User user) {
        return UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .build();
    }
}
