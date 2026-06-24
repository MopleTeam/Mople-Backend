package com.mople.dto.client.comment;

import com.mople.dto.response.meet.comment.PostCommentResponse;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostCommentClientResponse implements CommentClientResponse {
    private final Long commentId;
    private final Long version;
    private final String content;
    private final Long postId;
    private final Long parentId;
    private final Integer replyCount;
    private final Integer likeCount;
    private final boolean likedByMe;
    private final UserInfo writer;
    private final List<UserInfo> mentions;
    private final LocalDateTime time;

    public static List<PostCommentClientResponse> ofPostComments(List<PostCommentResponse> postCommentRespons) {
        return postCommentRespons.stream().map(PostCommentClientResponse::ofPostComment).toList();
    }

    public static PostCommentClientResponse ofPostComment(PostCommentResponse postCommentResponse) {
        return PostCommentClientResponse.builder()
                .commentId(postCommentResponse.commentId())
                .version(postCommentResponse.version())
                .content(postCommentResponse.content())
                .postId(postCommentResponse.postId())
                .parentId(postCommentResponse.parentId())
                .replyCount(postCommentResponse.replyCount())
                .likeCount(postCommentResponse.likeCount())
                .likedByMe(postCommentResponse.likedByMe())
                .writer(UserInfo.of(postCommentResponse.writer()))
                .mentions(ofMentions(postCommentResponse.mentions()))
                .time(postCommentResponse.time())
                .build();
    }

    private static List<UserInfo> ofMentions(List<User> mentions) {
        return mentions.stream()
                .map(UserInfo::of)
                .toList();
    }
}
