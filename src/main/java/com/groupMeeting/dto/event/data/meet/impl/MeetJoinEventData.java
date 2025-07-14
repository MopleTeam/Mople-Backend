package com.groupMeeting.dto.event.data.meet.impl;

import com.groupMeeting.dto.event.data.meet.MeetEventData;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class MeetJoinEventData implements MeetEventData {

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

    @Override
    public Long triggeredBy() {
        return newMemberId;
    }
}
