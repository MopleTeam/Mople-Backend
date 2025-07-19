package com.mople.user.service;

import com.mople.core.exception.custom.AuthException;
import com.mople.dto.request.user.RandomNicknameRequest;
import com.mople.dto.request.user.UserInfoRequest;
import com.mople.dto.response.user.UserInfoResponse;
import com.mople.entity.user.User;
import com.mople.global.enums.ExceptionReturnCode;
import com.mople.image.service.ImageService;
import com.mople.user.mapper.UserMapper;
import com.mople.user.repository.UserRepository;

import com.mople.user.repository.UserRepositorySupport;
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
