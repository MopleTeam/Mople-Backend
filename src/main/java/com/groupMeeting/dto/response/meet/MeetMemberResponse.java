package com.groupMeeting.dto.response.meet;

import com.groupMeeting.entity.meet.Meet;
import com.groupMeeting.entity.meet.MeetMember;

import java.util.List;

public record MeetMemberResponse(
        Long creatorId,
        List<MeetMemberListResponse> members
) {
    public MeetMemberResponse(Meet meet) {
        this(meet.getCreator().getId(), meet.getMembers().stream().map(MeetMemberListResponse::new).toList());
    }

    public record MeetMemberListResponse(
            Long memberId,
            String nickname,
            String profileImg
    ) {
        public MeetMemberListResponse(MeetMember member) {
            this(member.getUser().getId(), member.getUser().getNickname(), member.getUser().getProfileImg());
        }
    }
}