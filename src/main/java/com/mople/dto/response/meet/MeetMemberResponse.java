package com.mople.dto.response.meet;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetMemberResponse(
        UserInfo user
) {
    public static List<MeetMemberResponse> ofMemberList(List<MeetMember> members) {
        return members.stream().map(MeetMemberResponse::ofMember).toList();
    }

    private static MeetMemberResponse ofMember(MeetMember member) {
        return MeetMemberResponse.builder()
                .user(UserInfo.from(member.getUser()))
                .build();
    }
}
