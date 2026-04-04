package com.mople.dto.client;

import com.mople.entity.meet.notice.MeetNotice;
import com.mople.global.enums.notice.NoticeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class NoticeClientResponse {
    private final Long noticeId;
    private final Long version;
    private final Long meetId;
    private final NoticeType type;
    private final String content;
    private final boolean isPinned;
    private final LocalDateTime createdAt;

    public static List<NoticeClientResponse> ofNotices(List<MeetNotice> notices) {
        return notices.stream().map(NoticeClientResponse::ofNotice).toList();
    }

    public static NoticeClientResponse ofNotice(MeetNotice notice) {
        return NoticeClientResponse.builder()
                .noticeId(notice.getId())
                .version(notice.getVersion())
                .meetId(notice.getMeetId())
                .type(notice.getType())
                .content(notice.getContent())
                .isPinned(notice.isPinned())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
