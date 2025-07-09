package com.groupMeeting.entity.meet.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "comment_id")
    private Long commentId;

    @Builder
    public CommentLike(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
