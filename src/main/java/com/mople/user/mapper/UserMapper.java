package com.mople.user.mapper;

import com.mople.dto.response.user.UserInfoResponse;
import com.mople.entity.user.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.nickname", target = "nickname")
    @Mapping(source = "user.profileImg", target = "image")
    @Mapping(source = "user.badgeCount", target = "badgeCount")
    UserInfoResponse toUserResponse(User user);

}