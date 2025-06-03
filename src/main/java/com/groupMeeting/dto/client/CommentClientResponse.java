package com.groupMeeting.dto.client;

import com.groupMeeting.dto.response.meet.comment.CommentResponse;
import com.groupMeeting.dto.response.meet.comment.CommentUpdateResponse;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentClientResponse {
    private final Long commentId;
    private final Long postId;
    private final Long writerId;
    private final String writerName;
    private final String writerImage;
    private final String content;
    private final LocalDateTime time;

    public static List<CommentClientResponse> ofComments(List<CommentResponse> commentResponses) {
        return commentResponses.stream().map(CommentClientResponse::ofComment).toList();
    }

    public static CommentClientResponse ofComment(CommentResponse commentResponse) {
        return CommentClientResponse.builder()
                .commentId(commentResponse.commentId())
                .postId(commentResponse.postId())
                .writerId(commentResponse.writerId())
                .writerName(commentResponse.writerName())
                .writerImage(commentResponse.writerImage())
                .content(commentResponse.content())
                .time(commentResponse.time())
                .build();
    }

    public static CommentClientResponse ofUpdate(CommentUpdateResponse updateResponse) {
        return CommentClientResponse.builder()
                .commentId(updateResponse.commentId())
                .postId(updateResponse.postId())
                .writerId(updateResponse.writerId())
                .writerName(updateResponse.writerName())
                .writerImage(updateResponse.writerImage())
                .content(updateResponse.content())
                .time(updateResponse.time())
                .build();
    }
}
