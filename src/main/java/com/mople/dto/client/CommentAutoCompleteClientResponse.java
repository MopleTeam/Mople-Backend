package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CommentAutoCompleteClientResponse {
    private final UserInfo user;

    public static List<CommentAutoCompleteClientResponse> ofTargets(List<MeetMember> members){
        return members.stream().map(CommentAutoCompleteClientResponse::ofTarget).toList();
    }

    private static CommentAutoCompleteClientResponse ofTarget(MeetMember member){
        return CommentAutoCompleteClientResponse.builder()
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
