package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AutoCompleteClientResponse {
    private final UserInfo user;

    public static List<AutoCompleteClientResponse> ofTargets(List<MeetMember> members){
        return members.stream().map(AutoCompleteClientResponse::ofTarget).toList();
    }

    public static AutoCompleteClientResponse ofTarget(MeetMember member){
        return AutoCompleteClientResponse.builder()
                .user(UserInfo.from(member.getUser()))
                .build();
    }
}
