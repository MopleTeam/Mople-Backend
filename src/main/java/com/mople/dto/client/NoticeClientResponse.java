package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.notice.MeetNotice;
import com.mople.global.enums.notice.NoticeType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class NoticeClientResponse {
    private final Long noticeId;
    private final Long version;
    private final Long meetId;
    private final NoticeType type;
    private final String content;
    private final UserInfo writer;
    private final boolean isPinned;
    private final LocalDateTime createdAt;

    public static List<NoticeClientResponse> ofNotices(List<MeetNotice> notices, Map<Long, UserInfo> userInfoById) {
        return notices.stream()
                .map(notice -> {
                    Long creatorId = notice.getCreatorId();
                    UserInfo writerInfo = creatorId == null
                            ? null
                            : userInfoById.get(creatorId);

                    return ofNotice(notice, writerInfo);
                })
                .toList();
    }

    public static NoticeClientResponse ofNotice(MeetNotice notice, UserInfo writerInfo) {
        return NoticeClientResponse.builder()
                .noticeId(notice.getId())
                .version(notice.getVersion())
                .meetId(notice.getMeetId())
                .type(notice.getType())
                .content(notice.getContent())
                .writer(writerInfo)
                .isPinned(notice.isPinned())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
