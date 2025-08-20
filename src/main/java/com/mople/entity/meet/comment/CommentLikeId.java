package com.mople.entity.meet.comment;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
public class CommentLikeId implements Serializable {

    private Long commentId;
    private Long userId;

    public CommentLikeId(Long commentId, Long userId) {
        this.commentId = commentId;
        this.userId = userId;
    }
}
