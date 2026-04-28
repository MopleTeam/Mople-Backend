package com.mople.entity.meet.comment;

import com.mople.global.enums.CommentTarget;
import com.mople.global.enums.Status;

import jakarta.persistence.*;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meet_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "comment_id")
    private Long id;

    @Version
    private Long version;

    @Size(max = 2000)
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 30)
    private CommentTarget target;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

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
    private MeetComment(
            String content, CommentTarget target, Long targetId,
            Long parentId, LocalDateTime writeTime, Long writerId
    ) {

        this.content = content;
        this.target = target;
        this.targetId = targetId;
        this.parentId = parentId;
        this.writeTime = writeTime;
        this.status = Status.ACTIVE;
        this.writerId = writerId;
    }

    public static MeetComment ofNotice(
            String content, Long noticeId,
            LocalDateTime writeTime, Long writerId
    ) {

        return MeetComment.builder()
                .content(content)
                .target(CommentTarget.NOTICE)
                .targetId(noticeId)
                .writeTime(writeTime)
                .writerId(writerId)
                .build();
    }

    public static MeetComment ofParent(
            String content, Long postId,
            LocalDateTime writeTime, Long writerId
    ) {

        return MeetComment.builder()
                .content(content)
                .target(CommentTarget.POST)
                .targetId(postId)
                .writeTime(writeTime)
                .writerId(writerId)
                .build();
    }

    public static MeetComment ofChild(
            String content, Long postId,
            Long parentId, LocalDateTime writeTime, Long writerId
    ) {

        return MeetComment.builder()
                .content(content)
                .target(CommentTarget.POST)
                .targetId(postId)
                .parentId(parentId)
                .writeTime(writeTime)
                .writerId(writerId)
                .build();
    }

    public boolean isWriter(Long userId) {
        return this.writerId.equals(userId);
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
