package com.groupMeeting.meet.mapper;

import com.groupMeeting.dto.response.meet.MeetInviteResponse;
import com.groupMeeting.entity.meet.*;

import org.mapstruct.Mapper;

import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MeetMapper {
    MeetMapper INSTANCE = Mappers.getMapper(MeetMapper.class);
//
//    @Named("getMeetInfo")
//    @Mapping(source = "id", target = "meetId")
//    @Mapping(source = "creator.id", target = "creatorId")
//    @Mapping(source = "creator.nickname", target = "creatorNickname")
//    @Mapping(source = "meetMember.user.id", target = "name")
//    @Mapping(source = "meetMember.user.profileImg", target = "profileImage")
//    MeetInfoResponse getMeetInfo(Meet meet);
//
//    @IterableMapping(qualifiedByName = "getMeetInfo")
//    List<MeetInfoResponse> getMeetInfos(List<Meet> meets);

    MeetInviteResponse getInviteInfo(MeetInvite meetInvite);

//    @Mapping(source = "meeting.id", target = "id")
//    @Mapping(source = "meeting.name", target = "name")
//    @Mapping(source = "meeting.createdAt", target = "createdAt")
//    @Mapping(source = "meeting.creator", target = "creatorId", qualifiedByName = "getUserId")
//    @Mapping(source = "meeting.creator", target = "creatorNickname", qualifiedByName = "getUserNickname")
//    GetMeetingDetailDto toGetDetailDto(Meet meeting, MeetPlan latestActivePlan, MeetPlan latestClosedPlan);
}
