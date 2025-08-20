package com.mople.entity.meet.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(CommentLikeId.class)
public class CommentLike {

    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Builder
    public CommentLike(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
