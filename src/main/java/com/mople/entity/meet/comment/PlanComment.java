package com.mople.entity.meet.comment;

import com.mople.global.enums.Status;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "comment_id")
    private Long id;

    @Version
    private Long version;

    @Column(name = "content", nullable = false, length = 700)
    private String content;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "parent_id", updatable = false)
    private Long parentId;

    @Column(name = "reply_count")
    private Integer replyCount = null;

    @Column(name = "write_at", nullable = false)
    private LocalDateTime writeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Builder
    private PlanComment(String content, Long postId, Long parentId, Integer replyCount,
                       LocalDateTime writeTime, Status status, Long writerId) {
        this.content = content;
        this.postId = postId;
        this.parentId = parentId;
        this.replyCount = replyCount;
        this.writeTime = writeTime;
        this.status = status;
        this.writerId = writerId;
    }

    public static PlanComment ofParent(String content, Long postId, LocalDateTime writeTime, Status status, Long writerId) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .replyCount(0)
                .writeTime(writeTime)
                .status(status)
                .writerId(writerId)
                .build();
    }

    public static PlanComment ofChild(String content, Long postId, Long parentId, LocalDateTime writeTime, Status status, Long writerId) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .parentId(parentId)
                .writeTime(writeTime)
                .status(status)
                .writerId(writerId)
                .build();
    }

    public boolean matchWriter(Long userId) {
        return !this.writerId.equals(userId);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean canDecreaseReplyCount() {
        return this.replyCount > 0;
    }

    public boolean canDecreaseLikeCount() {
        return this.likeCount > 0;
    }

    public boolean isChildComment() {
        return parentId != null;
    }
}
