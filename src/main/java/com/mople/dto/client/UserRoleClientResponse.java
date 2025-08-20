package com.mople.dto.client;

import com.mople.entity.meet.MeetMember;
import com.mople.entity.meet.plan.PlanParticipant;
import com.mople.entity.user.User;
import com.mople.global.enums.UserRole;
import lombok.Builder;

import java.util.List;

@Builder
public record UserRoleClientResponse(
        Long userId,
        String nickname,
        String image,
        UserRole role
) {
    public static List<UserRoleClientResponse> ofParticipants(List<PlanParticipant> participants, Long hostId, Long creatorId) {
        return participants.stream()
                .map(p -> ofUser(p.getUser(), hostId, creatorId))
                .toList();
    }

    public static List<UserRoleClientResponse> ofAutoCompleteUsers(List<MeetMember> members, Long hostId, Long creatorId) {
        return members.stream()
                .map(m -> ofUser(m.getUser(), hostId, creatorId))
                .toList();
    }

    public static List<UserRoleClientResponse> ofMembers(List<MeetMember> members, Long hostId) {
        return members.stream()
                .map(m -> ofUser(m.getUser(), hostId))
                .toList();
    }

    private static UserRoleClientResponse ofUser(User user, Long hostId, Long creatorId) {
        return UserRoleClientResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .role(UserRole.getRole(user.getId(), hostId, creatorId))
                .build();
    }

    private static UserRoleClientResponse ofUser(User user, Long hostId) {
        return UserRoleClientResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .role(UserRole.getRole(user.getId(), hostId))
                .build();
    }
}
