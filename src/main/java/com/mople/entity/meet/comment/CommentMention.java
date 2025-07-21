package com.mople.entity.meet.comment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_mention")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentMention {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "comment_id")
    private Long commentId;

    @Builder
    public CommentMention(Long userId, Long commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
