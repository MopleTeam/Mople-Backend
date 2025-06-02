package com.groupMeeting.entity.meet.comment;

import com.groupMeeting.global.enums.Status;

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

    @Column(name = "content", nullable = false, length = 700)
    private String content;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "write_at", nullable = false)
    private LocalDateTime writeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @Column(name = "writer_id", updatable = false)
    private Long writerId;

    @Column(name = "writer_nickname", updatable = false)
    private String writerNickname;

    @Column(name = "writer_image", updatable = false)
    private String writerImg;

    @Builder
    public PlanComment(String content, LocalDateTime writeTime, Status status, Long postId, Long writerId, String writerNickname, String writerImg) {
        this.content = content;
        this.postId = postId;
        this.writeTime = writeTime;
        this.status = status;
        this.writerId = writerId;
        this.writerNickname = writerNickname;
        this.writerImg = writerImg;
    }

    public boolean matchWriter(Long userId) {
        return !this.writerId.equals(userId);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
