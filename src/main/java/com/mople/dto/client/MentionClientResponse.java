package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MentionClientResponse {
    private final UserInfo user;

    public static List<MentionClientResponse> ofTargets(List<MeetMember> members){
        return members.stream().map(MentionClientResponse::ofTarget).toList();
    }

    public static MentionClientResponse ofTarget(MeetMember member){
        return MentionClientResponse.builder()
                .user(
                        UserInfo.builder()
                                .userId(member.getUser().getId())
                                .nickname(member.getUser().getNickname())
                                .image(member.getUser().getProfileImg())
                                .build()
                )
                .build();
    }
}
