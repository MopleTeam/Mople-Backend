package com.groupMeeting.entity.meet.comment;

import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.Status;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanComment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "comment_id")
    private Long id;

    @Column(name = "content", nullable = false, length = 700)
    private String content;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "parent_id", updatable = false)
    private Long parentId;

    @Column(name = "reply_count")
    private Integer replyCount = null;

    @Column(name = "like_count")
    private int likeCount = 0;

    @Column(name = "liked_by_me")
    private boolean likedByMe = false;

    @Column(name = "write_at", nullable = false)
    private LocalDateTime writeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User writer;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentMention> mentions = new ArrayList<>();

    @Builder
    private PlanComment(String content, Long postId, Long parentId, Integer replyCount,
                       LocalDateTime writeTime, Status status, User writer) {
        this.content = content;
        this.postId = postId;
        this.parentId = parentId;
        this.replyCount = replyCount;
        this.writeTime = writeTime;
        this.status = status;
        this.writer = writer;
    }

    public static PlanComment ofParent(String content, Long postId, LocalDateTime writeTime, Status status, User writer) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .replyCount(0)
                .writeTime(writeTime)
                .status(status)
                .writer(writer)
                .build();
    }

    public static PlanComment ofChild(String content, Long postId, Long parentId, LocalDateTime writeTime, Status status, User writer) {
        return PlanComment.builder()
                .content(content)
                .postId(postId)
                .parentId(parentId)
                .writeTime(writeTime)
                .status(status)
                .writer(writer)
                .build();
    }

    public void addMention(CommentMention mention) {
        mention.updateComment(this);
        this.mentions.add(mention);
    }

    public boolean matchWriter(Long userId) {
        return !this.writer.getId().equals(userId);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void increaseReplyCount() {
        this.replyCount += 1;
    }

    public void decreaseReplyCount() {
        this.replyCount -= 1;
    }

    public void increaseLikeCount() {
        this.likeCount += 1;
    }

    public void decreaseLikeCount() {
        this.likeCount -= 1;
    }

    public void toggleLike() {
        if (likedByMe) {
            decreaseLikeCount();
        }
        if (!likedByMe) {
            increaseLikeCount();
        }

        this.likedByMe = !likedByMe;
    }
}
