package com.groupMeeting.dto.client;

import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.dto.response.meet.comment.CommentUpdateResponse;

import com.groupMeeting.dto.response.user.UserInfo;
import com.groupMeeting.entity.meet.comment.CommentMention;
import com.groupMeeting.entity.user.User;
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
    private final int likeCount;
    private final boolean likeByMe;
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
                .likeByMe(commentResponse.likedByMe())
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
                .likeByMe(updateResponse.likedByMe())
                .writer(ofWriter(updateResponse.writer()))
                .mentions(ofMentions(updateResponse.mentions()))
                .time(updateResponse.time())
                .build();
    }

    private static UserInfo ofWriter(User writer) {
        return UserInfo.builder()
                .userId(writer.getId())
                .nickname(writer.getNickname())
                .image(writer.getProfileImg())
                .build();
    }

    private static List<UserInfo> ofMentions(List<CommentMention> mentions) {
        return mentions.stream()
                .map((mention) ->
                        UserInfo.builder()
                                .userId(mention.getMentionedUser().getId())
                                .nickname(mention.getMentionedUser().getNickname())
                                .image(mention.getMentionedUser().getProfileImg())
                                .build()
                )
                .toList();
    }
}
