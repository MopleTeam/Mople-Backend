package com.mople.dto.client;

import com.mople.dto.response.user.UserInfo;
import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.global.enums.UserRole;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record UserRoleClientResponse(
        Long userId,
        String nickname,
        String image,
        UserRole role
) {
    public static List<UserRoleClientResponse> ofParticipants(
            List<PlanParticipant> participants,
            Map<Long, UserInfo> usersById,
            Long hostId,
            Long creatorId
    ) {
        return participants.stream()
                .map(p -> ofUser(usersById.get(p.getUserId()), hostId, creatorId))
                .toList();
    }

    public static List<UserRoleClientResponse> ofAutoCompleteUsers(
            List<MeetMember> members,
            Map<Long, UserInfo> usersById,
            Long hostId,
            Long creatorId
    ) {
        return members.stream()
                .map(m -> ofUser(usersById.get(m.getUserId()), hostId, creatorId))
                .toList();
    }

    public static List<UserRoleClientResponse> ofMembers(
            List<MeetMember> members,
            Map<Long, UserInfo> usersById,
            Long hostId
    ) {
        return members.stream()
                .map(m -> ofUser(usersById.get(m.getUserId()), hostId))
                .toList();
    }

    private static UserRoleClientResponse ofUser(UserInfo userInfo, Long hostId, Long creatorId) {
        return UserRoleClientResponse.builder()
                .userId(userInfo.userId())
                .nickname(userInfo.nickname())
                .image(userInfo.image())
                .role(UserRole.getRole(userInfo.userId(), hostId, creatorId))
                .build();
    }

    private static UserRoleClientResponse ofUser(UserInfo userInfo, Long hostId) {
        return UserRoleClientResponse.builder()
                .userId(userInfo.userId())
                .nickname(userInfo.nickname())
                .image(userInfo.image())
                .role(UserRole.getRole(userInfo.userId(), hostId))
                .build();
    }
}
