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
    private Long parentId = null;

    @Column(name = "write_at", nullable = false)
    private LocalDateTime writeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @Column(name = "writer_id")
    private Long writerId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    private PlanComment(String content, Long postId, Long parentId,
                       LocalDateTime writeTime, Long writerId) {
        this.content = content;
        this.postId = postId;
        this.parentId = parentId;
        this.writeTime = writeTime;
        this.status = Status.ACTIVE;
        this.writerId = writerId;
    }

    public static PlanComment ofParent(String content, Long postId, LocalDateTime writeTime, Long writerId) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .writeTime(writeTime)
                .writerId(writerId)
                .build();
    }

    public static PlanComment ofChild(String content, Long postId, Long parentId, LocalDateTime writeTime, Long writerId) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .parentId(parentId)
                .writeTime(writeTime)
                .writerId(writerId)
                .build();
    }

    public boolean matchWriter(Long userId) {
        return !this.writerId.equals(userId);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean isChildComment() {
        return parentId != null;
    }

    public void softDelete(Long deletedBy) {
        if (status == Status.DELETED) {
            return;
        }

        this.status = Status.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
