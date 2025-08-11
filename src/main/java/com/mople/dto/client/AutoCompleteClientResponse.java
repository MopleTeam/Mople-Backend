package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import com.mople.global.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AutoCompleteClientResponse {
    private final UserInfo user;

    public static List<AutoCompleteClientResponse> ofTargets(List<MeetMember> members, Long creatorId, Long hostId){
        return members.stream()
                .map(p -> ofTarget(p, UserRole.getRole(p.getId(), creatorId, hostId)))
                .toList();
    }

    public static AutoCompleteClientResponse ofTarget(MeetMember member, UserRole role){
        return AutoCompleteClientResponse.builder()
                .user(UserInfo.from(member.getUser(), role))
                .build();
    }
}
