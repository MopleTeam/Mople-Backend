package com.groupMeeting.user.service;

import com.groupMeeting.core.exception.custom.AuthException;
import com.groupMeeting.dto.request.user.RandomNicknameRequest;
import com.groupMeeting.dto.request.user.UserInfoRequest;
import com.groupMeeting.dto.response.user.UserInfoResponse;
import com.groupMeeting.entity.user.User;
import com.groupMeeting.global.enums.ExceptionReturnCode;
import com.groupMeeting.image.service.ImageService;
import com.groupMeeting.user.mapper.UserMapper;
import com.groupMeeting.user.repository.UserRepository;

import com.groupMeeting.user.repository.UserRepositorySupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final RandomNicknameRequest randomNickname = new RandomNicknameRequest();
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final UserRepositorySupport userRepositorySupport;

    @Transactional(readOnly = true)
    public UserInfoResponse getInfo(Long id) {
        return userMapper.toUserResponse(findUser(id));
    }

    @Transactional
    public UserInfoResponse updateInfo(Long id, UserInfoRequest updateInfo) {
        User user = findUser(id);

        if (user.imageValid()) {
            imageService.deleteImage(user.getProfileImg());
        }

        user.updateImageAndNickname(updateInfo.image(), updateInfo.nickname());

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void removeUser(final Long id) {
        userRepositorySupport.removeUser(id);
    }

    @Transactional(readOnly = true)
    public String randomUserNickname() {
        String nickName;

        do {
            nickName = randomNickname.getRandomName();
        } while (duplicateNickname(nickName));

        return nickName;
    }

    @Transactional(readOnly = true)
    public Boolean duplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new AuthException(ExceptionReturnCode.NOT_USER)
        );
    }
}
