package com.mople.dto.event.data.notify.meet;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.dto.response.notification.NotificationPayload;
import com.mople.global.enums.event.NotifyType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class MeetJoinNotifyEvent implements NotifyEvent {

    private final Long meetId;
    private final String meetName;
    private final Long newMemberId;
    private final String newMemberNickname;

    @Override
    public NotificationPayload payload() {
        return new NotificationPayload(
                meetName + "의 새 멤버 \uD83C\uDF89",
                newMemberNickname + "님이 가입했어요!"
        );
    }

    @Override
    public Map<String, String> routing() {
        return Map.of("meetId", meetId.toString());
    }

    @Override
    public NotifyType notifyType() {
        return NotifyType.MEET_NEW_MEMBER;
    }
}
