package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import com.mople.global.enums.UserRole;
import lombok.Builder;

import java.util.List;

@Builder
public record MeetMemberClientResponse(
        UserInfo user
) {
    public static List<MeetMemberClientResponse> ofMemberList(List<MeetMember> members, Long creatorId) {
        return members.stream()
                .map(m -> ofMember(m, UserRole.getRole(m.getId(), creatorId)))
                .toList();
    }

    private static MeetMemberClientResponse ofMember(MeetMember member, UserRole role) {
        return MeetMemberClientResponse.builder()
                .user(UserInfo.from(member.getUser(), role))
                .build();
    }
}
