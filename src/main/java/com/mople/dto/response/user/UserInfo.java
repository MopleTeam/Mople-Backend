package com.mople.dto.response.user;

import com.mople.entity.user.User;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record UserInfo(
        Long userId,
        String nickname,
        String image
) {
    public static UserInfo of(User user) {
        return UserInfo.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .image(user.getProfileImg())
                .build();
    }

    public static Map<Long, UserInfo> ofMap(List<User> users) {
        List<UserInfo> userInfos = users.stream().map(UserInfo::of).toList();

        return userInfos.stream()
                .collect(Collectors.toMap(UserInfo::userId, Function.identity()));
    }
}
