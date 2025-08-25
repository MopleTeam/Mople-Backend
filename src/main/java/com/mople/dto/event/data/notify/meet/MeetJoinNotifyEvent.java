package com.mople.dto.event.data.notify.meet;

import com.mople.dto.event.data.notify.NotifyEvent;
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
    public String getTitle() {
        return meetName + "의 새 멤버 \uD83C\uDF89";
    }

    @Override
    public String getBody() {
        return newMemberNickname + "님이 가입했어요!";
    }

    @Override
    public Map<String, String> getRoutingKey() {
        return Map.of("meetId", meetId.toString());
    }
}
