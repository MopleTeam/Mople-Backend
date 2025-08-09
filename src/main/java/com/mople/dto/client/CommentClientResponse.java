package com.mople.dto.client;

import com.mople.dto.response.meet.comment.CommentResponse;
import com.mople.dto.response.meet.comment.CommentUpdateResponse;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import com.mople.global.enums.UserRole;
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

    public static List<CommentClientResponse> ofComments(List<CommentResponse> commentResponses, Long creatorId, Long hostId) {
        return commentResponses.stream().map(c -> ofComment(c, creatorId, hostId)).toList();
    }

    public static CommentClientResponse ofComment(CommentResponse commentResponse, Long creatorId, Long hostId) {
        return CommentClientResponse.builder()
                .commentId(commentResponse.commentId())
                .content(commentResponse.content())
                .postId(commentResponse.postId())
                .parentId(commentResponse.parentId())
                .replyCount(commentResponse.replyCount())
                .likeCount(commentResponse.likeCount())
                .likedByMe(commentResponse.likedByMe())
                .writer(ofWriter(commentResponse.writer(), creatorId, hostId))
                .mentions(ofMentions(commentResponse.mentions(), creatorId, hostId))
                .time(commentResponse.time())
                .build();
    }

    public static CommentClientResponse ofUpdate(CommentUpdateResponse updateResponse, Long creatorId, Long hostId) {
        return CommentClientResponse.builder()
                .commentId(updateResponse.commentId())
                .content(updateResponse.content())
                .postId(updateResponse.postId())
                .parentId(updateResponse.parentId())
                .replyCount(updateResponse.replyCount())
                .likeCount(updateResponse.likeCount())
                .likedByMe(updateResponse.likedByMe())
                .writer(ofWriter(updateResponse.writer(), creatorId, hostId))
                .mentions(ofMentions(updateResponse.mentions(), creatorId, hostId))
                .time(updateResponse.time())
                .build();
    }

    private static UserInfo ofWriter(User writer, Long creatorId, Long hostId) {
        return UserInfo.from(writer, UserRole.getRole(writer.getId(), creatorId, hostId));
    }

    private static List<UserInfo> ofMentions(List<User> mentions, Long creatorId, Long hostId) {
        return mentions.stream()
                .map(m -> UserInfo.from(m, UserRole.getRole(m.getId(), creatorId, hostId)))
                .toList();
    }
}
