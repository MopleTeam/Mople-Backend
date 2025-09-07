package com.mople.entity.meet.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentStats {

    @Id
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "reply_count")
    private Integer replyCount = null;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount;

    @Builder
    private CommentStats(Long commentId, Integer replyCount, Integer likeCount) {
        this.commentId = commentId;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
    }

    public static CommentStats ofParent(Long commentId) {
        return CommentStats.builder()
                .commentId(commentId)
                .replyCount(0)
                .likeCount(0)
                .build();
    }

    public static CommentStats ofChild(Long commentId) {
        return CommentStats.builder()
                .commentId(commentId)
                .replyCount(null)
                .likeCount(0)
                .build();
    }

    public boolean canDecreaseReplyCount() {
        return this.replyCount > 0;
    }

    public boolean canDecreaseLikeCount() {
        return this.likeCount > 0;
    }
}
