package com.mople.dto.client.comment;

import com.mople.dto.response.user.UserInfo;

import java.time.LocalDateTime;

public interface CommentClientResponse {
    Long getCommentId();
    Long getVersion();
    String getContent();
    Long getParentId();
    UserInfo getWriter();
    LocalDateTime getTime();
}
