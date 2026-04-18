package com.mople.entity.meet.notice;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.global.enums.Status;
import com.mople.global.enums.notice.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "meet_notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "notice_id")
    private Long id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NoticeType type;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "meet_id", nullable = false)
    private Long meetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Builder
    public MeetNotice(NoticeType type, String content, Long creatorId, Long meetId) {
        this.type = type;
        this.content = content;
        this.creatorId = creatorId;
        this.meetId = meetId;
        this.pinnedAt = null;
        this.status = Status.ACTIVE;
    }

    public static MeetNotice ofCustom(String content, Long creatorId, Long meetId) {
        return MeetNotice.builder()
                .type(NoticeType.CUSTOM)
                .content(content)
                .creatorId(creatorId)
                .meetId(meetId)
                .build();
    }

    public static MeetNotice ofSystem(String content, Long meetId) {
        return MeetNotice.builder()
                .type(NoticeType.SYSTEM)
                .content(content)
                .creatorId(null)
                .meetId(meetId)
                .build();
    }

    public void updateNotice(String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        this.content = content;
    }

    public boolean isPinned() {
        return this.pinnedAt != null;
    }

    public void pin() {
        if (this.pinnedAt != null) {
            return;
        }

        this.pinnedAt = LocalDateTime.now();
    }

    public void unpin() {
        if (this.pinnedAt == null) {
            return;
        }

        this.pinnedAt = null;
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
