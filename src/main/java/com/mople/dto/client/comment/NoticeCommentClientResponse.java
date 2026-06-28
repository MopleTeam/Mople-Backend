package com.mople.dto.client.comment;

import com.mople.dto.response.meet.comment.NoticeCommentResponse;

import com.mople.dto.response.user.UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeCommentClientResponse {

    private final Long commentId;
    private final Long version;
    private final String content;
    private final Long parentId;
    private final UserInfo writer;
    private final LocalDateTime time;
    private final Long noticeId;

    public static NoticeCommentClientResponse ofNoticeComment(NoticeCommentResponse response) {
        return NoticeCommentClientResponse.builder()
                .commentId(response.commentId())
                .version(response.version())
                .content(response.content())
                .parentId(response.parentId())
                .writer(UserInfo.of(response.writer()))
                .time(response.time())
                .noticeId(response.noticeId())
                .build();
    }

    public static List<NoticeCommentClientResponse> ofNoticeComments(List<NoticeCommentResponse> responses) {
        return responses.stream()
                .map(NoticeCommentClientResponse::ofNoticeComment)
                .toList();
    }
}