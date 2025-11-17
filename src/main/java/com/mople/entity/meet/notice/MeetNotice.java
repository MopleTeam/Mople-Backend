package com.mople.entity.meet.notice;

import com.mople.entity.common.BaseTimeEntity;
import com.mople.global.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "meet_id", nullable = false)
    private Long meetId;

    @Builder
    public MeetNotice(NoticeType type, String content, Long creatorId, Long meetId) {
        this.type = type;
        this.content = content;
        this.creatorId = creatorId;
        this.meetId = meetId;
    }

    public static MeetNotice ofCustom(String content, Long creatorId, Long meetId) {
        return MeetNotice.builder()
                .type(NoticeType.HOST_CUSTOM)
                .content(content)
                .creatorId(creatorId)
                .meetId(meetId)
                .build();
    }

    public static MeetNotice ofSystem(NoticeType type, String content, Long meetId) {
        return MeetNotice.builder()
                .type(type)
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
}
